package com.mediacontrol.floatingwidget.permissions

import com.mediacontrol.floatingwidget.model.CapabilityGrantState

class PermissionReadinessSnapshotProvider(
    private val overlayPermissionChecker: OverlayPermissionChecker,
    private val notificationListenerAccessChecker: NotificationListenerAccessChecker,
    private val notificationPermissionChecker: NotificationPermissionChecker
) {
    fun createSnapshot(): PermissionReadinessSnapshot {
        return PermissionReadinessSnapshot(
            overlayAccess = overlayPermissionChecker.isGranted().toCapabilityState(),
            notificationListenerAccess = notificationListenerAccessChecker.isGranted().toCapabilityState(),
            notificationPermissionGranted = notificationPermissionChecker.isPermissionGranted(),
            notificationsEnabled = notificationPermissionChecker.areNotificationsEnabled(),
            serviceStartReadiness = CapabilityGrantState.Granted
        )
    }

    private fun Boolean.toCapabilityState(): CapabilityGrantState {
        return if (this) CapabilityGrantState.Granted else CapabilityGrantState.Missing
    }
}
