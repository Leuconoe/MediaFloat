package com.mediacontrol.floatingwidget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mediacontrol.floatingwidget.permissions.NotificationListenerAccessChecker
import com.mediacontrol.floatingwidget.permissions.NotificationPermissionChecker
import com.mediacontrol.floatingwidget.permissions.OverlayPermissionChecker
import com.mediacontrol.floatingwidget.permissions.OverlayReadinessResolver
import com.mediacontrol.floatingwidget.permissions.PermissionReadinessSnapshotProvider
import com.mediacontrol.floatingwidget.ui.AppShell
import com.mediacontrol.floatingwidget.ui.theme.MediaControlFloatingWidgetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val snapshotProvider = PermissionReadinessSnapshotProvider(
            overlayPermissionChecker = OverlayPermissionChecker(this),
            notificationListenerAccessChecker = NotificationListenerAccessChecker(this),
            notificationPermissionChecker = NotificationPermissionChecker(this)
        )
        val readinessResolver = OverlayReadinessResolver()
        val snapshot = snapshotProvider.createSnapshot()
        val capabilityState = readinessResolver.resolveCapabilities(snapshot)
        val runtimeState = readinessResolver.resolveRuntimeState(snapshot)

        enableEdgeToEdge()
        setContent {
            MediaControlFloatingWidgetTheme {
                AppShell(
                    capabilityState = capabilityState,
                    runtimeState = runtimeState
                )
            }
        }
    }
}
