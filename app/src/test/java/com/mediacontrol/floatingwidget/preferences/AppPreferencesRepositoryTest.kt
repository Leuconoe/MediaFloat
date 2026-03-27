package sw2.io.mediafloat.preferences

import sw2.io.mediafloat.model.AppPreferences
import sw2.io.mediafloat.storage.TestPreferencesStorage
import org.junit.Assert.assertEquals
import org.junit.Test

class AppPreferencesRepositoryTest {

    @Test
    fun setDebugToolsEnabled_persistsAndNotifiesListeners() {
        val repository = AppPreferencesRepository(
            AppPreferencesStore(TestPreferencesStorage())
        )
        val receivedStates = mutableListOf<AppPreferences>()

        repository.addListener(AppPreferencesListener { preferences ->
            receivedStates += preferences
        })

        repository.setDebugToolsEnabled(true)

        assertEquals(
            listOf(
                AppPreferences(debugToolsEnabled = false),
                AppPreferences(debugToolsEnabled = true)
            ),
            receivedStates
        )
        assertEquals(AppPreferences(debugToolsEnabled = true), repository.currentState())
    }
}
