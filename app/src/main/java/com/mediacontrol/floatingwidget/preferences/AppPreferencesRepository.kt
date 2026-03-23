package com.mediacontrol.floatingwidget.preferences

import com.mediacontrol.floatingwidget.model.AppPreferences

fun interface AppPreferencesListener {
    fun onAppPreferencesChanged(preferences: AppPreferences)
}

class AppPreferencesRepository(
    private val store: AppPreferencesStore
) {

    private val listeners = linkedSetOf<AppPreferencesListener>()

    @Volatile
    private var preferences: AppPreferences = store.load()

    fun currentState(): AppPreferences = preferences

    fun addListener(listener: AppPreferencesListener) {
        listeners += listener
        listener.onAppPreferencesChanged(preferences)
    }

    fun removeListener(listener: AppPreferencesListener) {
        listeners -= listener
    }

    @Synchronized
    fun setDebugToolsEnabled(enabled: Boolean) {
        save(preferences.copy(debugToolsEnabled = enabled))
    }

    @Synchronized
    fun reload() {
        preferences = store.load()
        notifyListeners()
    }

    private fun save(updatedPreferences: AppPreferences) {
        store.save(updatedPreferences)
        preferences = updatedPreferences
        notifyListeners()
    }

    private fun notifyListeners() {
        val snapshot = preferences
        listeners.forEach { it.onAppPreferencesChanged(snapshot) }
    }
}
