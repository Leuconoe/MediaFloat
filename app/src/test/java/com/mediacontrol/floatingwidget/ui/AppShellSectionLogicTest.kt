package sw2.io.mediafloat.ui

import sw2.io.mediafloat.model.MediaSessionState
import sw2.io.mediafloat.model.OverlayRuntimeState
import sw2.io.mediafloat.model.OverlayUnavailableReason
import sw2.io.mediafloat.model.WidgetAnchor
import sw2.io.mediafloat.model.WidgetLayout
import sw2.io.mediafloat.model.WidgetPosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppShellSectionLogicTest {

    @Test
    fun appSections_keepsLandingSettingsSupportOrderWhenDebugDisabled() {
        val sections = appSections(debugToolsEnabled = false)

        assertEquals(
            listOf(AppSection.Landing, AppSection.Settings, AppSection.Advanced, AppSection.Support),
            sections
        )
        assertFalse(AppSection.Debug in sections)
    }

    @Test
    fun appSections_addsDebugWhenPreferenceIsEnabled() {
        val sections = appSections(debugToolsEnabled = true)

        assertEquals(
            listOf(AppSection.Landing, AppSection.Settings, AppSection.Advanced, AppSection.Support, AppSection.Debug),
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
