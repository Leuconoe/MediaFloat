package sw2.io.mediafloat.runtime

import android.content.Context
import sw2.io.mediafloat.R
import sw2.io.mediafloat.model.CapabilityGrantState
import sw2.io.mediafloat.model.CapabilityState
import sw2.io.mediafloat.model.NotificationPosture
import sw2.io.mediafloat.model.OverlayRuntimeState
import sw2.io.mediafloat.model.OverlayUnavailableReason

data class RuntimeStatusSummary(
    val title: String,
    val detail: String,
    val capabilityLines: List<String>
)

object RuntimeStatusFormatter {
    fun format(
        context: Context,
        capabilityState: CapabilityState,
        runtimeState: OverlayRuntimeState
    ): RuntimeStatusSummary {
        return RuntimeStatusSummary(
            title = runtimeTitle(context, runtimeState),
            detail = runtimeDetail(context, runtimeState),
            capabilityLines = listOf(
                context.getString(R.string.runtime_capability_overlay_access, capabilityState.overlayAccess.label(context)),
                context.getString(R.string.runtime_capability_notification_listener, capabilityState.notificationListenerAccess.label(context)),
                context.getString(R.string.runtime_capability_notification_posture, capabilityState.notificationPosture.label(context)),
                context.getString(R.string.runtime_capability_service_start, capabilityState.serviceStartReadiness.label(context))
            )
        )
    }

    private fun runtimeTitle(context: Context, runtimeState: OverlayRuntimeState): String {
        return when (runtimeState) {
            OverlayRuntimeState.Ready -> context.getString(R.string.runtime_title_ready)
            is OverlayRuntimeState.Showing -> context.getString(R.string.runtime_title_showing)
            is OverlayRuntimeState.Suspended -> context.getString(R.string.runtime_title_suspended)
            is OverlayRuntimeState.Unavailable -> context.getString(R.string.runtime_title_unavailable)
        }
    }

    private fun runtimeDetail(context: Context, runtimeState: OverlayRuntimeState): String {
        return when (runtimeState) {
            OverlayRuntimeState.Ready -> context.getString(R.string.runtime_detail_ready)
            is OverlayRuntimeState.Showing -> context.getString(R.string.runtime_detail_showing)
            is OverlayRuntimeState.Suspended -> context.getString(
                R.string.runtime_detail_suspended,
                runtimeState.reason.label(context)
            )
            is OverlayRuntimeState.Unavailable -> context.getString(
                R.string.runtime_detail_unavailable,
                runtimeState.reason.label(context).lowercase()
            )
        }
    }

    private fun CapabilityGrantState.label(context: Context): String {
        return when (this) {
            CapabilityGrantState.Granted -> context.getString(R.string.state_granted)
            CapabilityGrantState.Missing -> context.getString(R.string.state_missing)
            CapabilityGrantState.Blocked -> context.getString(R.string.state_blocked)
            CapabilityGrantState.Unsupported -> context.getString(R.string.state_unsupported)
        }
    }

    private fun NotificationPosture.label(context: Context): String {
        return when (this) {
            NotificationPosture.Visible -> context.getString(R.string.state_visible)
            NotificationPosture.PermissionRequired -> context.getString(R.string.state_permission_required)
            NotificationPosture.Blocked -> context.getString(R.string.state_blocked)
        }
    }

    private fun OverlayUnavailableReason.label(context: Context): String {
        return when (this) {
            OverlayUnavailableReason.MissingOverlayAccess -> context.getString(R.string.recovery_goal_overlay_access)
            OverlayUnavailableReason.MissingNotificationListenerAccess -> context.getString(R.string.recovery_goal_listener_access)
            OverlayUnavailableReason.NotificationPostureBlocked -> context.getString(R.string.recovery_goal_notifications)
            OverlayUnavailableReason.ServiceStartNotAllowed -> context.getString(R.string.recovery_goal_service_start)
            OverlayUnavailableReason.UnsupportedDeviceCondition -> context.getString(R.string.recovery_goal_device_condition)
            OverlayUnavailableReason.UnknownRuntimeFailure -> context.getString(R.string.recovery_goal_runtime_issue)
        }
    }
}
