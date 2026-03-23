package com.mediacontrol.floatingwidget.widget

import com.mediacontrol.floatingwidget.model.WidgetButton
import com.mediacontrol.floatingwidget.model.WidgetConfig
import com.mediacontrol.floatingwidget.model.WidgetLayout
import com.mediacontrol.floatingwidget.model.WidgetSizePreset
import com.mediacontrol.floatingwidget.storage.TestPreferencesStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetConfigStoreTest {

    @Test
    fun load_returnsSavedConfigFromPreferencesStorage() {
        val storage = TestPreferencesStorage()
        val store = WidgetConfigStore(storage)
        val expected = WidgetConfig(
            layout = WidgetLayout(visibleButtons = setOf(WidgetButton.PlayPause, WidgetButton.Next)),
            sizePreset = WidgetSizePreset.Large,
            persistentOverlayEnabled = false
        )

        store.save(expected)

        assertEquals(expected, store.load())
    }

    @Test
    fun decodeVisibleButtons_fallsBackToDefaultForUnsupportedLayout() {
        val decoded = WidgetConfigStorageFormat.decodeVisibleButtons("Previous,Next")

        assertEquals(WidgetLayout.Default.visibleButtons, decoded)
        assertTrue(decoded.contains(WidgetButton.PlayPause))
    }
}
