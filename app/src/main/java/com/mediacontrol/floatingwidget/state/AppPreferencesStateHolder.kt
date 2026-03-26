package sw2.io.mediafloat.state

import sw2.io.mediafloat.model.AppPreferences
import sw2.io.mediafloat.model.AppLanguage
import sw2.io.mediafloat.preferences.AppPreferencesListener
import sw2.io.mediafloat.preferences.AppPreferencesRepository

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
