package com.mediacontrol.floatingwidget.overlay

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.mediacontrol.floatingwidget.MediaControlAppServices
import com.mediacontrol.floatingwidget.R
import com.mediacontrol.floatingwidget.MainActivity
import com.mediacontrol.floatingwidget.debug.DebugLogWriter
import com.mediacontrol.floatingwidget.media.AndroidMediaSessionRepository
import com.mediacontrol.floatingwidget.media.MediaControllerCommandDispatcher
import com.mediacontrol.floatingwidget.media.MediaSessionStateListener
import com.mediacontrol.floatingwidget.model.MediaSessionState
import com.mediacontrol.floatingwidget.model.OverlayRuntimeState
import com.mediacontrol.floatingwidget.model.WidgetConfig
import com.mediacontrol.floatingwidget.runtime.OverlayRuntimeCoordinator
import com.mediacontrol.floatingwidget.runtime.OverlayRuntimeRegistry
import com.mediacontrol.floatingwidget.widget.WidgetPreferencesListener
import com.mediacontrol.floatingwidget.widget.WidgetPreferencesRepository

class OverlayService : Service() {

    private lateinit var appServices: MediaControlAppServices
    private lateinit var runtimeCoordinator: OverlayRuntimeCoordinator
    private lateinit var positionStore: OverlayPositionStore
    private lateinit var mediaRepository: AndroidMediaSessionRepository
    private lateinit var mediaDispatcher: MediaControllerCommandDispatcher
    private lateinit var widgetPreferencesRepository: WidgetPreferencesRepository
    private lateinit var debugLogWriter: DebugLogWriter
    private lateinit var overlayHost: OverlayHost
    private var currentMediaState: MediaSessionState = MediaSessionState.Unavailable
    private var currentWidgetConfig: WidgetConfig = WidgetConfig()
    private var runtimeStarted = false
    private val mediaStateListener = MediaSessionStateListener { mediaState ->
        currentMediaState = mediaState
        if (runtimeStarted) {
            overlayHost.update(
                OverlayViewState(
                    config = currentWidgetConfig,
                    mediaState = mediaState
                )
            )
            publishShowingState()
        }
    }
    private val widgetPreferencesListener = WidgetPreferencesListener { preferencesState ->
        currentWidgetConfig = preferencesState.config
        if (runtimeStarted) {
            overlayHost.update(
                OverlayViewState(
                    config = preferencesState.config,
                    mediaState = currentMediaState
                )
            )
            publishShowingState(position = preferencesState.position)
        }
    }

    override fun onCreate() {
        super.onCreate()
        OverlayNotificationChannels.ensureCreated(this)
        appServices = MediaControlAppServices.from(this)
        debugLogWriter = appServices.debugLogRepository
        runtimeCoordinator = appServices.runtimeCoordinator
        positionStore = appServices.overlayPositionStore
        mediaRepository = appServices.mediaRepository
        mediaDispatcher = appServices.mediaCommandDispatcher
        widgetPreferencesRepository = appServices.widgetPreferencesRepository
        currentWidgetConfig = widgetPreferencesRepository.currentState().config
        overlayHost = WindowManagerOverlayHost(
            context = this,
            positionStore = positionStore,
            onMediaCommand = { command -> mediaDispatcher.dispatch(command) },
            onPositionChanged = {
                widgetPreferencesRepository.savePosition(it)
                publishShowingState(position = it)
            },
            debugLogWriter = debugLogWriter
        )
        mediaRepository.addListener(mediaStateListener)
        widgetPreferencesRepository.addListener(widgetPreferencesListener)
        debugLogWriter.info(TAG, "Overlay service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            Log.d(TAG, "Received explicit runtime stop")
            debugLogWriter.info(TAG, "Received explicit overlay stop action")
            stopRuntime()
            return START_NOT_STICKY
        }

        if (!runtimeCoordinator.capabilityState().isReadyForPersistentOverlay()) {
            val blockedState = runtimeCoordinator.readinessRuntimeState()
            OverlayRuntimeRegistry.update(blockedState)
            Log.w(TAG, "Overlay runtime blocked by readiness state: $blockedState")
            debugLogWriter.warn(TAG, "Overlay runtime blocked by readiness", blockedState.toString())
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundRuntime()
        ensureRuntimeAttached()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        mediaRepository.removeListener(mediaStateListener)
        widgetPreferencesRepository.removeListener(widgetPreferencesListener)
        stopRuntime(updateRegistry = true, stopSelfService = false)
        debugLogWriter.info(TAG, "Overlay service destroyed")
        super.onDestroy()
    }

    fun buildForegroundNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            10,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this,
            11,
            stopIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, OverlayNotificationChannels.RUNTIME_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(getString(R.string.overlay_runtime_notification_title))
            .setContentText(getString(R.string.overlay_runtime_notification_body))
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.overlay_runtime_notification_stop),
                stopIntent
            )
            .build()
    }

    private fun startForegroundRuntime() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildForegroundNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        )
    }

    private fun ensureRuntimeAttached() {
        if (!runtimeStarted) {
            mediaRepository.connect()
            runtimeStarted = true
        }

        currentMediaState = mediaRepository.currentState()
        overlayHost.attach(
            viewState = OverlayViewState(
                config = currentWidgetConfig,
                mediaState = currentMediaState
            ),
            position = widgetPreferencesRepository.currentState().position
        )
        publishShowingState()
        Log.d(TAG, "Overlay runtime attached with media state $currentMediaState")
        debugLogWriter.info(TAG, "Overlay runtime attached", "mediaState=$currentMediaState config=$currentWidgetConfig")
    }

    private fun publishShowingState(position: com.mediacontrol.floatingwidget.model.WidgetPosition = widgetPreferencesRepository.currentState().position) {
        OverlayRuntimeRegistry.update(
            OverlayRuntimeState.Showing(
                position = position,
                layout = currentWidgetConfig.layout,
                mediaState = currentMediaState
            )
        )
    }

    private fun stopRuntime(
        updateRegistry: Boolean = true,
        stopSelfService: Boolean = true
    ) {
        if (runtimeStarted) {
            mediaRepository.disconnect()
            runtimeStarted = false
        }
        overlayHost.detach()
        stopForeground(STOP_FOREGROUND_REMOVE)
        debugLogWriter.info(TAG, "Stopped overlay runtime")

        if (updateRegistry) {
            OverlayRuntimeRegistry.update(runtimeCoordinator.readinessRuntimeState())
        }

        if (stopSelfService) {
            stopSelf()
        }
    }

    companion object {
        const val ACTION_START = "com.mediacontrol.floatingwidget.action.START_OVERLAY_RUNTIME"
        const val ACTION_STOP = "com.mediacontrol.floatingwidget.action.STOP_OVERLAY_RUNTIME"
        private const val NOTIFICATION_ID = 41
        private const val TAG = "OverlayService"

        fun startIntent(context: Context): Intent {
            return Intent(context, OverlayService::class.java).setAction(ACTION_START)
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, OverlayService::class.java).setAction(ACTION_STOP)
        }
    }
}
