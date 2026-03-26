package sw2.io.mediafloat.overlay

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object OverlayNotificationChannels {
    const val RUNTIME_CHANNEL_ID = "overlay_runtime"

    fun ensureCreated(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            RUNTIME_CHANNEL_ID,
            context.getString(sw2.io.mediafloat.R.string.overlay_runtime_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(sw2.io.mediafloat.R.string.overlay_runtime_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }
}
