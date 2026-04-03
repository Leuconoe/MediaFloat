package sw2.io.mediafloat.model

enum class CapabilityGrantState {
    Granted,
    Missing,
    Blocked,
    Unsupported
}

enum class NotificationPosture {
    Visible,
    PermissionRequired,
    Blocked
}

enum class OverlayUnavailableReason {
    MissingOverlayAccess,
    MissingNotificationListenerAccess,
    NotificationPostureBlocked,
    ServiceStartNotAllowed,
    UnsupportedDeviceCondition,
    UnknownRuntimeFailure
}

data class CapabilityState(
    val overlayAccess: CapabilityGrantState,
    val notificationListenerAccess: CapabilityGrantState,
    val notificationPosture: NotificationPosture,
    val serviceStartReadiness: CapabilityGrantState,
    val automationLaunchReadiness: CapabilityGrantState = CapabilityGrantState.Granted
) {
    fun unavailableReasons(): List<OverlayUnavailableReason> {
        val reasons = mutableListOf<OverlayUnavailableReason>()

        when (overlayAccess) {
            CapabilityGrantState.Missing, CapabilityGrantState.Blocked -> {
                reasons += OverlayUnavailableReason.MissingOverlayAccess
            }
            CapabilityGrantState.Unsupported -> {
                reasons += OverlayUnavailableReason.UnsupportedDeviceCondition
            }
            CapabilityGrantState.Granted -> Unit
        }

        when (notificationListenerAccess) {
            CapabilityGrantState.Missing, CapabilityGrantState.Blocked -> {
                reasons += OverlayUnavailableReason.MissingNotificationListenerAccess
            }
            CapabilityGrantState.Unsupported -> {
                reasons += OverlayUnavailableReason.UnsupportedDeviceCondition
            }
            CapabilityGrantState.Granted -> Unit
        }

        if (notificationPosture != NotificationPosture.Visible) {
            reasons += OverlayUnavailableReason.NotificationPostureBlocked
        }

        when (serviceStartReadiness) {
            CapabilityGrantState.Missing, CapabilityGrantState.Blocked -> {
                reasons += OverlayUnavailableReason.ServiceStartNotAllowed
            }
            CapabilityGrantState.Unsupported -> {
                reasons += OverlayUnavailableReason.UnsupportedDeviceCondition
            }
            CapabilityGrantState.Granted -> Unit
        }

        if (automationLaunchReadiness == CapabilityGrantState.Unsupported) {
            reasons += OverlayUnavailableReason.UnsupportedDeviceCondition
        }

        return reasons.distinct()
    }

    fun isReadyForPersistentOverlay(): Boolean = unavailableReasons().isEmpty()

    fun hasAnyPermissionGranted(): Boolean {
        return overlayAccess == CapabilityGrantState.Granted ||
            notificationListenerAccess == CapabilityGrantState.Granted ||
            notificationPosture == NotificationPosture.Visible
    }
}
