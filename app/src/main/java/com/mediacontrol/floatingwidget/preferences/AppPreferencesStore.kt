package com.mediacontrol.floatingwidget.preferences

import android.content.Context
import com.mediacontrol.floatingwidget.model.AppPreferences
import com.mediacontrol.floatingwidget.model.AppLanguage
import com.mediacontrol.floatingwidget.storage.PreferencesStorage
import com.mediacontrol.floatingwidget.storage.SharedPreferencesStorage

class AppPreferencesStore(
    private val storage: PreferencesStorage
) {

    constructor(context: Context) : this(
        SharedPreferencesStorage(
            context.applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        )
    )

    fun load(): AppPreferences {
        val defaults = AppPreferences()
        return AppPreferences(
            debugToolsEnabled = storage.getBoolean(KEY_DEBUG_TOOLS_ENABLED, defaults.debugToolsEnabled),
            appLanguage = AppLanguage.fromStorageValue(
                storage.getString(KEY_APP_LANGUAGE, defaults.appLanguage.storageValue)
            )
        )
    }

    fun save(preferences: AppPreferences) {
        storage.edit {
            putBoolean(KEY_DEBUG_TOOLS_ENABLED, preferences.debugToolsEnabled)
            putString(KEY_APP_LANGUAGE, preferences.appLanguage.storageValue)
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "app_preferences"
        const val KEY_DEBUG_TOOLS_ENABLED = "debug_tools_enabled"
        const val KEY_APP_LANGUAGE = "app_language"
    }
}
