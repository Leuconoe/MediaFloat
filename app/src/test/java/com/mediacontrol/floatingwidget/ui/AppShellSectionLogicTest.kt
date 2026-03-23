package com.mediacontrol.floatingwidget.ui

import com.mediacontrol.floatingwidget.model.MediaSessionState
import com.mediacontrol.floatingwidget.model.OverlayRuntimeState
import com.mediacontrol.floatingwidget.model.OverlayUnavailableReason
import com.mediacontrol.floatingwidget.model.WidgetAnchor
import com.mediacontrol.floatingwidget.model.WidgetLayout
import com.mediacontrol.floatingwidget.model.WidgetPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppShellSectionLogicTest {

    @Test
    fun appSections_keepsLandingSettingsSupportOrderWhenDebugDisabled() {
        val sections = appSections(debugToolsEnabled = false)

        assertEquals(
            listOf(AppSection.Landing, AppSection.Settings, AppSection.Support),
            sections
        )
        assertFalse(AppSection.Debug in sections)
    }

    @Test
    fun appSections_addsDebugWhenPreferenceIsEnabled() {
        val sections = appSections(debugToolsEnabled = true)

        assertEquals(
            listOf(AppSection.Landing, AppSection.Settings, AppSection.Support, AppSection.Debug),
            sections
        )
        assertTrue(AppSection.Debug in sections)
    }

    @Test
    fun landingSupportingLine_guidesRecoveryWhenReadinessIsBlocked() {
        val line = landingSupportingLine(
            runtimeState = OverlayRuntimeState.Unavailable(OverlayUnavailableReason.MissingOverlayAccess),
            readinessProblems = listOf(OverlayUnavailableReason.MissingOverlayAccess)
        )

        assertEquals(
            "Recover the missing system access below, come back here, and try the overlay again.",
            line
        )
    }

    @Test
    fun landingStatusLabel_reportsShowingState() {
        val label = OverlayRuntimeState.Showing(
            position = WidgetPosition(anchor = WidgetAnchor.End, xOffsetDp = 16, yOffsetDp = 96),
            layout = WidgetLayout.Default,
            mediaState = MediaSessionState.Unavailable
        ).landingStatusLabel()

        assertEquals("Showing", label)
    }
}
