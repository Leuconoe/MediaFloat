package com.mediacontrol.floatingwidget.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.mediacontrol.floatingwidget.debug.DebugLogWriter
import com.mediacontrol.floatingwidget.debug.NoOpDebugLogWriter
import com.mediacontrol.floatingwidget.model.MediaCommand
import com.mediacontrol.floatingwidget.model.MediaSessionErrorReason
import com.mediacontrol.floatingwidget.model.MediaSessionLimitReason
import com.mediacontrol.floatingwidget.model.MediaSessionState
import com.mediacontrol.floatingwidget.model.PlaybackStatus

class AndroidMediaSessionRepository(
    context: Context,
    private val debugLogWriter: DebugLogWriter = NoOpDebugLogWriter
) : MediaSessionRepository {

    private val appContext = context.applicationContext
    private val mediaSessionManager =
        appContext.getSystemService(MediaSessionManager::class.java)
    private val listenerComponent = ComponentName(appContext, MediaNotificationListenerService::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val listeners = linkedSetOf<MediaSessionStateListener>()
    private val activeSessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        handleControllersChanged(controllers.orEmpty())
    }
    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            publishState(resolveState(currentController))
            if (shouldRecoverAfterPlaybackStateChange(state)) {
                requestRecovery("playback_state_${state?.state ?: "unknown"}")
            }
        }

        override fun onSessionDestroyed() {
            Log.d(TAG, "Active media session destroyed")
            requestRecovery("session_destroyed")
        }
    }

    @Volatile
    private var currentState: MediaSessionState = MediaSessionState.Unavailable

    @Volatile
    private var currentController: MediaController? = null

    private var connected = false
    private var connectionCount = 0
    private var recoveryRunnable: Runnable? = null

    @Synchronized
    override fun connect() {
        connectionCount += 1
        if (connected) {
            debugLogWriter.debug(TAG, "Media session repository already connected", "connectionCount=$connectionCount")
            return
        }

        try {
            mediaSessionManager.addOnActiveSessionsChangedListener(
                activeSessionsListener,
                listenerComponent
            )
            connected = true
            refresh("connect")
            Log.d(TAG, "Connected active media session listener")
            debugLogWriter.info(TAG, "Connected media session listener", "connectionCount=$connectionCount")
        } catch (securityException: SecurityException) {
            connectionCount = (connectionCount - 1).coerceAtLeast(0)
            Log.w(TAG, "Notification listener access missing for media sessions", securityException)
            debugLogWriter.warn(TAG, "Media session listener permission missing", securityException.message)
            publishState(MediaSessionState.Error(MediaSessionErrorReason.PermissionRevoked))
        } catch (throwable: Throwable) {
            connectionCount = (connectionCount - 1).coerceAtLeast(0)
            Log.e(TAG, "Failed to connect media session listener", throwable)
            debugLogWriter.error(TAG, "Failed to connect media session listener", throwable.message)
            publishState(MediaSessionState.Error(MediaSessionErrorReason.PlatformFailure))
        }
    }

    @Synchronized
    override fun disconnect() {
        connectionCount = (connectionCount - 1).coerceAtLeast(0)
        if (connectionCount > 0) {
            debugLogWriter.debug(TAG, "Keeping media session repository connected", "connectionCount=$connectionCount")
            return
        }

        if (!connected) {
            clearRecovery()
            unregisterController()
            return
        }

        runCatching {
            mediaSessionManager.removeOnActiveSessionsChangedListener(activeSessionsListener)
        }
        clearRecovery()
        unregisterController()
        connected = false
        publishState(MediaSessionState.Unavailable)
        Log.d(TAG, "Disconnected active media session listener")
        debugLogWriter.info(TAG, "Disconnected media session listener")
    }

    @Synchronized
    override fun prepareForOverlayActivation() {
        if (!connected) {
            return
        }

        requestRecovery("overlay_activation")
    }

    override fun currentState(): MediaSessionState = currentState

    override fun addListener(listener: MediaSessionStateListener) {
        listeners += listener
        listener.onMediaSessionStateChanged(currentState)
    }

    override fun removeListener(listener: MediaSessionStateListener) {
        listeners -= listener
    }

    fun currentController(): MediaController? = currentController

    @Synchronized
    fun refresh(reason: String = "manual"): MediaSessionState {
        debugLogWriter.debug(TAG, "Refreshing media sessions", "reason=$reason")
        val nextState = try {
            val controllers = mediaSessionManager.getActiveSessions(listenerComponent).orEmpty()
            selectController(controllers)?.let { controller ->
                registerController(controller)
                resolveState(controller)
            } ?: run {
                unregisterController()
                MediaSessionState.Unavailable
            }
        } catch (securityException: SecurityException) {
            Log.w(TAG, "Notification listener access missing while refreshing media sessions", securityException)
            debugLogWriter.warn(TAG, "Media session refresh permission missing", securityException.message)
            unregisterController()
            MediaSessionState.Error(MediaSessionErrorReason.PermissionRevoked)
        } catch (throwable: Throwable) {
            Log.e(TAG, "Failed to refresh media sessions", throwable)
            debugLogWriter.error(TAG, "Failed to refresh media sessions", throwable.message)
            unregisterController()
            MediaSessionState.Error(MediaSessionErrorReason.PlatformFailure)
        }

        publishState(nextState)
        return nextState
    }

    @Synchronized
    fun requestRecovery(reason: String) {
        if (!connected) {
            return
        }

        if (currentState != MediaSessionState.Discovering && currentState !is MediaSessionState.Active) {
            publishState(MediaSessionState.Discovering)
        }
        scheduleRecovery(reason = reason, attempt = 0)
    }

    @Synchronized
    fun onNotificationListenerConnected() {
        debugLogWriter.info(TAG, "Notification listener connected; refreshing tracked media session")
        requestRecovery("listener_connected")
    }

    @Synchronized
    fun onNotificationListenerDisconnected() {
        debugLogWriter.warn(TAG, "Notification listener disconnected; clearing pending media recovery")
        clearRecovery()
        publishState(MediaSessionState.Discovering)
    }

    private fun handleControllersChanged(controllers: List<MediaController>) {
        val nextController = selectController(controllers)
        if (nextController == null) {
            unregisterController()
            publishState(MediaSessionState.Unavailable)
            requestRecovery("active_sessions_empty")
            return
        }

        registerController(nextController)
        val nextState = resolveState(nextController)
        publishState(nextState)
        if (shouldContinueRecovery(nextState)) {
            requestRecovery("controller_state_changed")
        } else {
            clearRecovery()
        }
    }

    private fun selectController(controllers: List<MediaController>): MediaController? {
        if (controllers.isEmpty()) {
            return null
        }

        return controllers
            .sortedWith(
                compareByDescending<MediaController> { it.playbackState.isActivelyPlaying() }
                    .thenByDescending { it.playbackState?.lastPositionUpdateTime ?: 0L }
            )
            .firstOrNull()
    }

    private fun registerController(controller: MediaController) {
        if (currentController?.sessionToken == controller.sessionToken) {
            return
        }

        unregisterController()
        currentController = controller
        controller.registerCallback(controllerCallback)
        Log.d(TAG, "Tracking media controller for ${controller.packageName}")
        debugLogWriter.debug(TAG, "Tracking media controller", "package=${controller.packageName}")
    }

    private fun unregisterController() {
        currentController?.unregisterCallback(controllerCallback)
        currentController = null
    }

    private fun resolveState(controller: MediaController?): MediaSessionState {
        if (controller == null) {
            return MediaSessionState.Unavailable
        }

        val playbackState = controller.playbackState
            ?: return MediaSessionState.Limited(
                reason = MediaSessionLimitReason.PlaybackStateUnknown,
                supportedActions = emptySet()
            )

        val supportedActions = playbackState.actions.toSupportedCommands()
        val status = playbackState.state.toPlaybackStatus()

        return if (supportedActions.isEmpty()) {
            MediaSessionState.Limited(
                reason = MediaSessionLimitReason.MissingTransportControls,
                supportedActions = emptySet()
            )
        } else {
            MediaSessionState.Active(
                sessionId = "${controller.packageName}:${controller.sessionToken}",
                supportedActions = supportedActions,
                playbackStatus = status
            )
        }
    }

    private fun publishState(state: MediaSessionState) {
        if (currentState != state) {
            debugLogWriter.debug(TAG, "Media session state updated", "state=$state")
        }
        currentState = state
        listeners.forEach { it.onMediaSessionStateChanged(state) }
    }

    @Synchronized
    private fun scheduleRecovery(reason: String, attempt: Int) {
        clearRecovery()
        val runnable = Runnable {
            val nextState = refresh(reason = "recovery_${attempt + 1}_$reason")
            synchronized(this) {
                recoveryRunnable = null
                if (connected && attempt + 1 < MAX_RECOVERY_ATTEMPTS && shouldContinueRecovery(nextState)) {
                    scheduleRecovery(reason = reason, attempt = attempt + 1)
                }
            }
        }
        recoveryRunnable = runnable
        val delayMillis = if (attempt == 0) 0L else RECOVERY_RETRY_DELAY_MS
        handler.postDelayed(runnable, delayMillis)
    }

    @Synchronized
    private fun clearRecovery() {
        recoveryRunnable?.let(handler::removeCallbacks)
        recoveryRunnable = null
    }

    private fun shouldRecoverAfterPlaybackStateChange(state: PlaybackState?): Boolean {
        if (state == null) {
            return true
        }

        return when (state.state) {
            PlaybackState.STATE_NONE,
            PlaybackState.STATE_STOPPED,
            PlaybackState.STATE_ERROR -> true
            else -> state.actions.toSupportedCommands().isEmpty()
        }
    }

    private fun shouldContinueRecovery(state: MediaSessionState): Boolean {
        return when (state) {
            is MediaSessionState.Active -> false
            is MediaSessionState.Limited -> state.reason == MediaSessionLimitReason.PlaybackStateUnknown ||
                state.reason == MediaSessionLimitReason.MissingTransportControls
            MediaSessionState.Discovering,
            MediaSessionState.Unavailable -> true
            is MediaSessionState.Error -> false
        }
    }

    private fun Long.toSupportedCommands(): Set<MediaCommand> {
        val commands = linkedSetOf<MediaCommand>()

        if (this and PlaybackState.ACTION_SKIP_TO_PREVIOUS != 0L) {
            commands += MediaCommand.Previous
        }
        if (
            this and PlaybackState.ACTION_PLAY != 0L ||
            this and PlaybackState.ACTION_PAUSE != 0L ||
            this and PlaybackState.ACTION_PLAY_PAUSE != 0L
        ) {
            commands += MediaCommand.TogglePlayPause
        }
        if (this and PlaybackState.ACTION_SKIP_TO_NEXT != 0L) {
            commands += MediaCommand.Next
        }

        return commands
    }

    private fun Int.toPlaybackStatus(): PlaybackStatus {
        return when (this) {
            PlaybackState.STATE_PLAYING -> PlaybackStatus.Playing
            PlaybackState.STATE_PAUSED -> PlaybackStatus.Paused
            PlaybackState.STATE_BUFFERING,
            PlaybackState.STATE_CONNECTING,
            PlaybackState.STATE_FAST_FORWARDING,
            PlaybackState.STATE_REWINDING,
            PlaybackState.STATE_SKIPPING_TO_NEXT,
            PlaybackState.STATE_SKIPPING_TO_PREVIOUS,
            PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM -> PlaybackStatus.Buffering
            PlaybackState.STATE_STOPPED,
            PlaybackState.STATE_NONE,
            PlaybackState.STATE_ERROR -> PlaybackStatus.Stopped
            else -> PlaybackStatus.Unknown
        }
    }

    private fun PlaybackState?.isActivelyPlaying(): Boolean {
        return this?.state == PlaybackState.STATE_PLAYING || this?.state == PlaybackState.STATE_BUFFERING
    }

    private companion object {
        const val TAG = "MediaSessionRepo"
        const val MAX_RECOVERY_ATTEMPTS = 3
        const val RECOVERY_RETRY_DELAY_MS = 750L
    }
}
