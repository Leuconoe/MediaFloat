package sw2.io.mediafloat

import sw2.io.mediafloat.model.WidgetButton
import sw2.io.mediafloat.model.WidgetLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppShellTest {

    @Test
    fun supportedWidgetLayouts_keepPlayPauseAndExpectedDefaultOrder() {
        val supportedButtonSets = WidgetLayout.supportedButtonSets

        assertEquals(4, supportedButtonSets.size)
        assertTrue(supportedButtonSets.all { WidgetButton.PlayPause in it })
        assertEquals(
            listOf(WidgetButton.Previous, WidgetButton.PlayPause, WidgetButton.Next),
            WidgetLayout.Default.orderedButtons
        )
    }
}
