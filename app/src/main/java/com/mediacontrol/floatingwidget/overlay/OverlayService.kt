package com.mediacontrol.floatingwidget.overlay

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mediacontrol.floatingwidget.R

class OverlayService : Service() {

    override fun onCreate() {
        super.onCreate()
        OverlayNotificationChannels.ensureCreated(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, OverlayNotificationChannels.RUNTIME_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(getString(R.string.overlay_runtime_notification_title))
            .setContentText(getString(R.string.overlay_runtime_notification_body))
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "com.mediacontrol.floatingwidget.action.START_OVERLAY_RUNTIME"
        const val ACTION_STOP = "com.mediacontrol.floatingwidget.action.STOP_OVERLAY_RUNTIME"

        fun startIntent(context: Context): Intent {
            return Intent(context, OverlayService::class.java).setAction(ACTION_START)
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, OverlayService::class.java).setAction(ACTION_STOP)
        }
    }
}
