package sw2.io.mediafloat.preferences

import sw2.io.mediafloat.model.AppPreferences
import sw2.io.mediafloat.storage.TestPreferencesStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppPreferencesStoreTest {

    @Test
    fun load_returnsSafeDefaultsWhenNothingIsSaved() {
        val store = AppPreferencesStore(TestPreferencesStorage())

        assertFalse(store.load().debugToolsEnabled)
    }

    @Test
    fun load_returnsSavedPreferences() {
        val store = AppPreferencesStore(TestPreferencesStorage())
        val expected = AppPreferences(debugToolsEnabled = true)

        store.save(expected)

        assertEquals(expected, store.load())
    }
}
