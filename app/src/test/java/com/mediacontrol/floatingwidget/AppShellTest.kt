package com.mediacontrol.floatingwidget

import com.mediacontrol.floatingwidget.ui.defaultPlaceholderSections
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppShellTest {

    @Test
    fun defaultPlaceholderSections_matchPlannedAppBoundaries() {
        val sections = defaultPlaceholderSections()

        assertEquals(3, sections.size)
        assertEquals("Overlay runtime", sections[0].title)
        assertEquals("Media controls", sections[1].title)
        assertEquals("Settings and support", sections[2].title)
        assertTrue(sections.all { it.detail.isNotBlank() })
    }
}
