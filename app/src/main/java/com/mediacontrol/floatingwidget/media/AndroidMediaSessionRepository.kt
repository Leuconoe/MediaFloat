package sw2.io.mediafloat.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.util.Log
import sw2.io.mediafloat.debug.DebugLogWriter
import sw2.io.mediafloat.debug.NoOpDebugLogWriter
import sw2.io.mediafloat.model.MediaArtwork
import sw2.io.mediafloat.model.MediaCommand
import sw2.io.mediafloat.model.MediaSessionErrorReason
import sw2.io.mediafloat.model.MediaSessionLimitReason
import sw2.io.mediafloat.model.MediaSessionState
import sw2.io.mediafloat.model.PlaybackStatus

internal data class NotificationArtworkSnapshot(
    val packageName: String,
    val artwork: MediaArtwork.BitmapSource
)

class AndroidMediaSessionRepository(
    context: Context,
    private val debugLogWriter: DebugLogWriter = NoOpDebugLogWriter
) : MediaSessionRepository {

    private val appContext = context.applicationContext
    private val mediaSessionManager =
        appContext.getSystemService(MediaSessionManager::class.java)
    private val listenerComponent = ComponentName(appContext, MediaNotificationListenerService::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private val controllerSelector = MediaControllerSelector
    private val recoveryPolicy = MediaRecoveryPolicy
    private val artworkResolver = MediaArtworkCandidateResolver(appContext.contentResolver)
    private val listeners = linkedSetOf<MediaSessionStateListener>()
    private val activeSessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        handleControllersChanged(controllers.orEmpty())
    }
    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            publishState(resolveState(currentController))
            if (recoveryPolicy.shouldRecoverAfterPlaybackStateChange(state)) {
                requestRecovery("playback_state_${state?.state ?: "unknown"}")
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            publishState(resolveState(currentController))
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

    @Volatile
    private var notificationArtworkByPackage: Map<String, MediaArtwork.BitmapSource> = emptyMap()

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
        notificationArtworkByPackage = emptyMap()
        publishState(MediaSessionState.Discovering)
    }

    @Synchronized
    internal fun replaceNotificationArtworkSnapshot(snapshots: List<NotificationArtworkSnapshot>) {
        notificationArtworkByPackage = snapshots.associate { it.packageName to it.artwork }
        currentController?.let { publishState(resolveState(it)) }
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
        if (recoveryPolicy.shouldContinueRecovery(nextState)) {
            requestRecovery("controller_state_changed")
        } else {
            clearRecovery()
        }
    }

    private fun selectController(controllers: List<MediaController>): MediaController? = controllerSelector.select(controllers)

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

        val title = resolveMediaTitle(
            displayTitle = controller.metadata?.getText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE),
            title = controller.metadata?.getText(MediaMetadata.METADATA_KEY_TITLE)
        )
        val artist = controller.metadata?.getText(MediaMetadata.METADATA_KEY_ARTIST)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val artworkCandidates = resolveArtworkCandidates(controller)
        val sessionId = "${controller.packageName}:${controller.sessionToken}"
        val playbackState = controller.playbackState
            ?: return buildMediaSessionState(
                sessionId = sessionId,
                title = title,
                artist = artist,
                artworkCandidates = artworkCandidates,
                playbackStatus = null,
                supportedActions = null
            )

        return buildMediaSessionState(
            sessionId = sessionId,
            title = title,
            artist = artist,
            artworkCandidates = artworkCandidates,
            playbackStatus = playbackState.state.toPlaybackStatus(),
            supportedActions = playbackState.actions.toSupportedCommands()
        )
    }

    private fun resolveArtworkCandidates(controller: MediaController): List<MediaArtwork> {
        return artworkResolver.resolve(controller, notificationArtworkByPackage)
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
                if (connected && attempt + 1 < MAX_RECOVERY_ATTEMPTS && recoveryPolicy.shouldContinueRecovery(nextState)) {
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

    private fun shouldRecoverAfterPlaybackStateChange(state: PlaybackState?): Boolean = recoveryPolicy.shouldRecoverAfterPlaybackStateChange(state)

    private fun shouldContinueRecovery(state: MediaSessionState): Boolean = recoveryPolicy.shouldContinueRecovery(state)

    internal companion object {
        fun buildMediaSessionState(
            sessionId: String,
            title: String?,
            artist: String?,
            artworkCandidates: List<MediaArtwork> = emptyList(),
            playbackStatus: PlaybackStatus?,
            supportedActions: Set<MediaCommand>?
        ): MediaSessionState {
            val resolvedPlaybackStatus = playbackStatus
                ?: return MediaSessionState.Limited(
                    reason = MediaSessionLimitReason.PlaybackStateUnknown,
                    title = title,
                    artist = artist,
                    artworkCandidates = artworkCandidates,
                    supportedActions = emptySet()
                )

            val resolvedSupportedActions = supportedActions.orEmpty()

            return if (resolvedSupportedActions.isEmpty()) {
                MediaSessionState.Limited(
                    reason = MediaSessionLimitReason.MissingTransportControls,
                    title = title,
                    artist = artist,
                    artworkCandidates = artworkCandidates,
                    supportedActions = emptySet()
                )
            } else {
                MediaSessionState.Active(
                    sessionId = sessionId,
                    title = title,
                    artist = artist,
                    artworkCandidates = artworkCandidates,
                    supportedActions = resolvedSupportedActions,
                    playbackStatus = resolvedPlaybackStatus
                )
            }
        }

        fun buildArtworkCandidates(
            metadataDisplayIconUri: MediaArtwork.UriSource?,
            metadataArtUri: MediaArtwork.UriSource?,
            metadataAlbumArtUri: MediaArtwork.UriSource?,
            metadataDisplayIconBitmap: MediaArtwork.BitmapSource?,
            metadataArtBitmap: MediaArtwork.BitmapSource?,
            metadataAlbumArtBitmap: MediaArtwork.BitmapSource?,
            notificationLargeIcon: MediaArtwork.BitmapSource?
        ): List<MediaArtwork> {
            return listOfNotNull(
                metadataDisplayIconUri,
                metadataArtUri,
                metadataAlbumArtUri,
                metadataDisplayIconBitmap,
                metadataArtBitmap,
                metadataAlbumArtBitmap,
                notificationLargeIcon
            )
        }

        fun resolveMediaTitle(displayTitle: CharSequence?, title: CharSequence?): String? {
            return displayTitle.normalizedMediaTitle() ?: title.normalizedMediaTitle()
        }

        private fun CharSequence?.normalizedMediaTitle(): String? {
            return this
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
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

        const val TAG = "MediaSessionRepo"
        const val MAX_RECOVERY_ATTEMPTS = 3
        const val RECOVERY_RETRY_DELAY_MS = 750L
    }
}
