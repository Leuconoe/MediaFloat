package sw2.io.mediafloat.preferences

import sw2.io.mediafloat.model.AppPreferences
import sw2.io.mediafloat.model.AppLanguage

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
    fun setAppLanguage(appLanguage: AppLanguage) {
        save(preferences.copy(appLanguage = appLanguage))
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
