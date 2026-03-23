package com.mediacontrol.floatingwidget.preferences

import android.content.Context
import com.mediacontrol.floatingwidget.model.AppPreferences
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
            debugToolsEnabled = storage.getBoolean(KEY_DEBUG_TOOLS_ENABLED, defaults.debugToolsEnabled)
        )
    }

    fun save(preferences: AppPreferences) {
        storage.edit {
            putBoolean(KEY_DEBUG_TOOLS_ENABLED, preferences.debugToolsEnabled)
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "app_preferences"
        const val KEY_DEBUG_TOOLS_ENABLED = "debug_tools_enabled"
    }
}
