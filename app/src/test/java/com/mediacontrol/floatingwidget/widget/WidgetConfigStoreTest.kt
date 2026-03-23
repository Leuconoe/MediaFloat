package com.mediacontrol.floatingwidget.widget

import com.mediacontrol.floatingwidget.model.WidgetButton
import com.mediacontrol.floatingwidget.model.WidgetConfig
import com.mediacontrol.floatingwidget.model.WidgetLayout
import com.mediacontrol.floatingwidget.model.WidgetSizePreset
import com.mediacontrol.floatingwidget.model.WidgetThemePreset
import com.mediacontrol.floatingwidget.model.WidgetWidthStyle
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
            widthStyle = WidgetWidthStyle.Wide,
            themePreset = WidgetThemePreset.Pink,
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

    @Test
    fun load_fallsBackToDefaultWidthStyleAndThemePresetForUnknownValues() {
        val storage = TestPreferencesStorage()
        val store = WidgetConfigStore(storage)

        storage.edit {
            putString("width_style", "CinemaScope")
            putString("theme_preset", "NeonGreen")
        }

        val loaded = store.load()

        assertEquals(WidgetConfig().widthStyle, loaded.widthStyle)
        assertEquals(WidgetConfig().themePreset, loaded.themePreset)
    }
}
