package com.mediacontrol.floatingwidget.runtime

import com.mediacontrol.floatingwidget.model.CapabilityGrantState
import com.mediacontrol.floatingwidget.model.CapabilityState
import com.mediacontrol.floatingwidget.model.NotificationPosture
import com.mediacontrol.floatingwidget.model.OverlayRuntimeState
import com.mediacontrol.floatingwidget.model.OverlayUnavailableReason

data class RuntimeStatusSummary(
    val title: String,
    val detail: String,
    val capabilityLines: List<String>
)

object RuntimeStatusFormatter {
    fun format(
        capabilityState: CapabilityState,
        runtimeState: OverlayRuntimeState
    ): RuntimeStatusSummary {
        return RuntimeStatusSummary(
            title = runtimeTitle(runtimeState),
            detail = runtimeDetail(runtimeState),
            capabilityLines = listOf(
                "Overlay access: ${capabilityState.overlayAccess.label()}",
                "Notification listener: ${capabilityState.notificationListenerAccess.label()}",
                "Notification posture: ${capabilityState.notificationPosture.label()}",
                "Service start: ${capabilityState.serviceStartReadiness.label()}"
            )
        )
    }

    private fun runtimeTitle(runtimeState: OverlayRuntimeState): String {
        return when (runtimeState) {
            OverlayRuntimeState.Ready -> "Runtime ready"
            is OverlayRuntimeState.Showing -> "Overlay showing"
            is OverlayRuntimeState.Suspended -> "Runtime suspended"
            is OverlayRuntimeState.Unavailable -> "Runtime unavailable"
        }
    }

    private fun runtimeDetail(runtimeState: OverlayRuntimeState): String {
        return when (runtimeState) {
            OverlayRuntimeState.Ready -> {
                "Permissions and notification posture are aligned for the foreground service to show the overlay."
            }
            is OverlayRuntimeState.Showing -> {
                "The foreground service owns a live WindowManager overlay and is updating media command availability in place."
            }
            is OverlayRuntimeState.Suspended -> {
                "The runtime is paused until the owning foreground service can recover: ${runtimeState.reason.label()}."
            }
            is OverlayRuntimeState.Unavailable -> {
                "Persistent overlay mode is blocked until ${runtimeState.reason.label().lowercase()}."
            }
        }
    }

    private fun CapabilityGrantState.label(): String {
        return when (this) {
            CapabilityGrantState.Granted -> "granted"
            CapabilityGrantState.Missing -> "missing"
            CapabilityGrantState.Blocked -> "blocked"
            CapabilityGrantState.Unsupported -> "unsupported"
        }
    }

    private fun NotificationPosture.label(): String {
        return when (this) {
            NotificationPosture.Visible -> "visible"
            NotificationPosture.PermissionRequired -> "permission required"
            NotificationPosture.Blocked -> "blocked"
        }
    }

    private fun OverlayUnavailableReason.label(): String {
        return when (this) {
            OverlayUnavailableReason.MissingOverlayAccess -> "overlay access is granted"
            OverlayUnavailableReason.MissingNotificationListenerAccess -> "notification listener access is granted"
            OverlayUnavailableReason.NotificationPostureBlocked -> "notification visibility is restored"
            OverlayUnavailableReason.ServiceStartNotAllowed -> "service start is allowed"
            OverlayUnavailableReason.UnsupportedDeviceCondition -> "the device condition is supported"
            OverlayUnavailableReason.UnknownRuntimeFailure -> "the runtime issue is resolved"
        }
    }
}
