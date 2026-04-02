package sw2.io.mediafloat.media

import android.graphics.Bitmap
import android.graphics.Canvas
import android.service.notification.StatusBarNotification
import android.service.notification.NotificationListenerService
import android.util.Log
import sw2.io.mediafloat.MediaControlAppServices
import sw2.io.mediafloat.model.MediaArtwork
import sw2.io.mediafloat.model.MediaArtworkSource

class MediaNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
        publishNotificationArtworkSnapshot()
        MediaControlAppServices.from(this).mediaRepository.onNotificationListenerConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
        MediaControlAppServices.from(this).mediaRepository.onNotificationListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        publishNotificationArtworkSnapshot()
        MediaControlAppServices.from(this).mediaRepository.requestRecovery(
            reason = "notification_posted_${sbn?.packageName ?: "unknown"}"
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        publishNotificationArtworkSnapshot()
        MediaControlAppServices.from(this).mediaRepository.requestRecovery(
            reason = "notification_removed_${sbn?.packageName ?: "unknown"}"
        )
    }

    private fun publishNotificationArtworkSnapshot() {
        val snapshots = activeNotifications.orEmpty().mapNotNull(::resolveNotificationArtworkSnapshot)
        MediaControlAppServices.from(this).mediaRepository.replaceNotificationArtworkSnapshot(snapshots)
    }

    private fun resolveNotificationArtworkSnapshot(sbn: StatusBarNotification): NotificationArtworkSnapshot? {
        val largeIconBitmap = sbn.notification.getLargeIcon()?.loadBitmap() ?: return null

        return NotificationArtworkSnapshot(
            packageName = sbn.packageName,
            artwork = MediaArtwork.BitmapSource(
                source = MediaArtworkSource.NotificationLargeIcon,
                bitmap = largeIconBitmap,
                widthPx = largeIconBitmap.width,
                heightPx = largeIconBitmap.height
            )
        )
    }

    private fun android.graphics.drawable.Icon.loadBitmap(): Bitmap? {
        val drawable = loadDrawable(this@MediaNotificationListenerService) ?: return null
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: drawable.minimumWidth.takeIf { it > 0 } ?: 1
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: drawable.minimumHeight.takeIf { it > 0 } ?: 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private companion object {
        const val TAG = "MediaNotifListener"
    }
}
