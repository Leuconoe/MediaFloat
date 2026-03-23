package com.mediacontrol.floatingwidget.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityStateTest {

    @Test
    fun readyState_hasNoUnavailableReasons() {
        val capabilityState = CapabilityState(
            overlayAccess = CapabilityGrantState.Granted,
            notificationListenerAccess = CapabilityGrantState.Granted,
            notificationPosture = NotificationPosture.Visible,
            serviceStartReadiness = CapabilityGrantState.Granted
        )

        assertTrue(capabilityState.unavailableReasons().isEmpty())
        assertTrue(capabilityState.isReadyForPersistentOverlay())
    }

    @Test
    fun blockedState_collectsMissingRequirementsInPriorityOrder() {
        val capabilityState = CapabilityState(
            overlayAccess = CapabilityGrantState.Missing,
            notificationListenerAccess = CapabilityGrantState.Blocked,
            notificationPosture = NotificationPosture.Blocked,
            serviceStartReadiness = CapabilityGrantState.Blocked
        )

        assertEquals(
            listOf(
                OverlayUnavailableReason.MissingOverlayAccess,
                OverlayUnavailableReason.MissingNotificationListenerAccess,
                OverlayUnavailableReason.NotificationPostureBlocked,
                OverlayUnavailableReason.ServiceStartNotAllowed
            ),
            capabilityState.unavailableReasons()
        )
        assertFalse(capabilityState.isReadyForPersistentOverlay())
    }
}
