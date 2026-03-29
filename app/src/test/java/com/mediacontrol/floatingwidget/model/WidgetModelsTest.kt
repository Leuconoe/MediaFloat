package sw2.io.mediafloat.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetModelsTest {

    @Test
    fun orderedButtons_followWidgetButtonOrderForSupportedLayout() {
        val layout = WidgetLayout(
            visibleButtons = setOf(WidgetButton.PlayPause, WidgetButton.Next)
        )

        assertEquals(listOf(WidgetButton.PlayPause, WidgetButton.Next), layout.orderedButtons)
        assertTrue(layout.isSupported())
    }

    @Test(expected = IllegalArgumentException::class)
    fun unsupportedLayout_requiresPlannedButtonSet() {
        WidgetLayout(
            visibleButtons = setOf(WidgetButton.Previous, WidgetButton.Next)
        )
    }

    @Test
    fun overlayAppearance_expandsHorizontalSizingForWideWidthStyle() {
        val regular = WidgetConfig(
            sizePreset = WidgetSizePreset.Standard,
            widthStyle = WidgetWidthStyle.Regular
        ).overlayAppearance()
        val wide = WidgetConfig(
            sizePreset = WidgetSizePreset.Standard,
            widthStyle = WidgetWidthStyle.Wide
        ).overlayAppearance()

        assertEquals(regular.sizing.buttonHeightDp, regular.sizing.buttonWidthDp)
        assertTrue(wide.sizing.buttonWidthDp > wide.sizing.buttonHeightDp)
        assertTrue(wide.sizing.handleWidthDp > regular.sizing.handleWidthDp)
        assertTrue(wide.sizing.containerWidthDp > regular.sizing.containerWidthDp)
    }

    @Test
    fun overlayAppearance_keepsTitleStripWidthLockedToContainerWidthAcrossPresets() {
        WidgetSizePreset.entries.forEach { preset ->
            WidgetWidthStyle.entries.forEach { widthStyle ->
                val sizing = WidgetConfig(sizePreset = preset, widthStyle = widthStyle).overlayAppearance().sizing

                assertEquals(sizing.containerWidthDp, sizing.titleStripWidthDp)
                assertEquals(sizing.containerHeightDp, sizing.thumbnailSizeDp)
                assertTrue(sizing.titleStripMinHeightDp > 0)
                assertTrue(sizing.titleStripHorizontalPaddingDp > 0)
            }
        }
    }

    @Test
    fun overlayAppearance_returnsThemeSpecificColors() {
        val darkBlue = WidgetConfig(themePreset = WidgetThemePreset.DarkBlue).overlayAppearance()
        val pink = WidgetConfig(themePreset = WidgetThemePreset.Pink).overlayAppearance()

        assertEquals(0xE6122237.toInt(), darkBlue.colors.surfaceColor)
        assertEquals(0xFFF3F8FF.toInt(), darkBlue.colors.iconEnabledColor)
        assertEquals(0xF0BB5C80.toInt(), pink.colors.surfaceColor)
        assertNotEquals(darkBlue.colors.surfaceColor, pink.colors.surfaceColor)
    }
}
