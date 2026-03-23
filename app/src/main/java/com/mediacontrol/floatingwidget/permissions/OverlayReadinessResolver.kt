package com.mediacontrol.floatingwidget.permissions

import com.mediacontrol.floatingwidget.model.CapabilityGrantState
import com.mediacontrol.floatingwidget.model.CapabilityState
import com.mediacontrol.floatingwidget.model.NotificationPosture
import com.mediacontrol.floatingwidget.model.OverlayRuntimeState
import com.mediacontrol.floatingwidget.model.OverlayUnavailableReason

data class PermissionReadinessSnapshot(
    val overlayAccess: CapabilityGrantState,
    val notificationListenerAccess: CapabilityGrantState,
    val notificationPermissionGranted: Boolean,
    val notificationsEnabled: Boolean,
    val serviceStartReadiness: CapabilityGrantState,
    val automationLaunchReadiness: CapabilityGrantState = CapabilityGrantState.Granted
)

class NotificationPostureResolver {
    fun resolve(
        notificationPermissionGranted: Boolean,
        notificationsEnabled: Boolean
    ): NotificationPosture {
        return when {
            !notificationsEnabled -> NotificationPosture.Blocked
            !notificationPermissionGranted -> NotificationPosture.PermissionRequired
            else -> NotificationPosture.Visible
        }
    }
}

class OverlayReadinessResolver(
    private val notificationPostureResolver: NotificationPostureResolver = NotificationPostureResolver()
) {
    fun resolveCapabilities(snapshot: PermissionReadinessSnapshot): CapabilityState {
        return CapabilityState(
            overlayAccess = snapshot.overlayAccess,
            notificationListenerAccess = snapshot.notificationListenerAccess,
            notificationPosture = notificationPostureResolver.resolve(
                notificationPermissionGranted = snapshot.notificationPermissionGranted,
                notificationsEnabled = snapshot.notificationsEnabled
            ),
            serviceStartReadiness = snapshot.serviceStartReadiness,
            automationLaunchReadiness = snapshot.automationLaunchReadiness
        )
    }

    fun resolveRuntimeState(snapshot: PermissionReadinessSnapshot): OverlayRuntimeState {
        val capabilityState = resolveCapabilities(snapshot)
        val reasons = capabilityState.unavailableReasons()

        return if (reasons.isEmpty()) {
            OverlayRuntimeState.Ready
        } else {
            OverlayRuntimeState.Unavailable(reasons.firstOrNull() ?: OverlayUnavailableReason.UnknownRuntimeFailure)
        }
    }
}
