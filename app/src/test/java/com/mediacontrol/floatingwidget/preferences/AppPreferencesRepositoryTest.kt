package com.mediacontrol.floatingwidget.preferences

import com.mediacontrol.floatingwidget.model.AppPreferences
import com.mediacontrol.floatingwidget.storage.TestPreferencesStorage
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
