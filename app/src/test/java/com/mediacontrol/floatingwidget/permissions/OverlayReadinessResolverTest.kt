package sw2.io.mediafloat.permissions

import sw2.io.mediafloat.model.CapabilityGrantState
import sw2.io.mediafloat.model.NotificationPosture
import sw2.io.mediafloat.model.OverlayRuntimeState
import sw2.io.mediafloat.model.OverlayUnavailableReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayReadinessResolverTest {

    private val resolver = OverlayReadinessResolver()

    @Test
    fun resolveCapabilities_marksNotificationPermissionGap() {
        val snapshot = PermissionReadinessSnapshot(
            overlayAccess = CapabilityGrantState.Granted,
            notificationListenerAccess = CapabilityGrantState.Granted,
            notificationPermissionGranted = false,
            notificationsEnabled = true,
            serviceStartReadiness = CapabilityGrantState.Granted
        )

        val capabilityState = resolver.resolveCapabilities(snapshot)

        assertEquals(NotificationPosture.PermissionRequired, capabilityState.notificationPosture)
        assertEquals(
            listOf(OverlayUnavailableReason.NotificationPostureBlocked),
            capabilityState.unavailableReasons()
        )
    }

    @Test
    fun resolveRuntimeState_returnsReadyWhenAllCapabilitiesPass() {
        val snapshot = PermissionReadinessSnapshot(
            overlayAccess = CapabilityGrantState.Granted,
            notificationListenerAccess = CapabilityGrantState.Granted,
            notificationPermissionGranted = true,
            notificationsEnabled = true,
            serviceStartReadiness = CapabilityGrantState.Granted
        )

        val runtimeState = resolver.resolveRuntimeState(snapshot)

        assertTrue(runtimeState === OverlayRuntimeState.Ready)
    }

    @Test
    fun resolveRuntimeState_prioritizesOverlayAccessFailure() {
        val snapshot = PermissionReadinessSnapshot(
            overlayAccess = CapabilityGrantState.Missing,
            notificationListenerAccess = CapabilityGrantState.Granted,
            notificationPermissionGranted = true,
            notificationsEnabled = true,
            serviceStartReadiness = CapabilityGrantState.Granted
        )

        val runtimeState = resolver.resolveRuntimeState(snapshot)

        assertEquals(
            OverlayRuntimeState.Unavailable(OverlayUnavailableReason.MissingOverlayAccess),
            runtimeState
        )
    }
}
