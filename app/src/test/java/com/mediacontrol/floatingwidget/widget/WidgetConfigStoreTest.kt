package sw2.io.mediafloat.widget

import sw2.io.mediafloat.model.WidgetButton
import sw2.io.mediafloat.model.WidgetConfig
import sw2.io.mediafloat.model.WidgetLayout
import sw2.io.mediafloat.model.WidgetSizePreset
import sw2.io.mediafloat.model.WidgetThemePreset
import sw2.io.mediafloat.model.WidgetWidthStyle
import sw2.io.mediafloat.storage.TestPreferencesStorage
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
            persistentOverlayEnabled = false,
            allowLowQualityThumbnailFallback = true
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

    @Test
    fun load_keepsLowQualityThumbnailFallbackDisabledByDefault() {
        val store = WidgetConfigStore(TestPreferencesStorage())

        assertEquals(false, store.load().allowLowQualityThumbnailFallback)
    }
}
