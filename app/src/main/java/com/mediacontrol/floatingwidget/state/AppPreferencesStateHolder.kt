package com.mediacontrol.floatingwidget.state

import com.mediacontrol.floatingwidget.model.AppPreferences
import com.mediacontrol.floatingwidget.model.AppLanguage
import com.mediacontrol.floatingwidget.preferences.AppPreferencesListener
import com.mediacontrol.floatingwidget.preferences.AppPreferencesRepository

class AppPreferencesStateHolder(
    private val repository: AppPreferencesRepository
) : ObservableStateHolder<AppPreferences>(repository.currentState()) {

    private val listener = AppPreferencesListener { preferences ->
        updateState(preferences)
    }

    init {
        repository.addListener(listener)
    }

    fun setDebugToolsEnabled(enabled: Boolean) {
        repository.setDebugToolsEnabled(enabled)
    }

    fun setAppLanguage(appLanguage: AppLanguage) {
        repository.setAppLanguage(appLanguage)
    }

    fun close() {
        repository.removeListener(listener)
    }
}
