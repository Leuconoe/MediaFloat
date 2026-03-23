package com.mediacontrol.floatingwidget.media

import android.service.notification.StatusBarNotification
import android.service.notification.NotificationListenerService
import android.util.Log
import com.mediacontrol.floatingwidget.MediaControlAppServices

class MediaNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected")
        MediaControlAppServices.from(this).mediaRepository.onNotificationListenerConnected()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener disconnected")
        MediaControlAppServices.from(this).mediaRepository.onNotificationListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        MediaControlAppServices.from(this).mediaRepository.requestRecovery(
            reason = "notification_posted_${sbn?.packageName ?: "unknown"}"
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        MediaControlAppServices.from(this).mediaRepository.requestRecovery(
            reason = "notification_removed_${sbn?.packageName ?: "unknown"}"
        )
    }

    private companion object {
        const val TAG = "MediaNotifListener"
    }
}
