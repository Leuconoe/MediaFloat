package com.mediacontrol.floatingwidget.model

import org.junit.Assert.assertEquals
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
}
