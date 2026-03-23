package com.mediacontrol.floatingwidget.storage

import android.content.SharedPreferences

interface PreferencesStorage {
    fun getString(key: String, defaultValue: String?): String?

    fun getInt(key: String, defaultValue: Int): Int

    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun edit(block: PreferencesStorageEditor.() -> Unit)
}

interface PreferencesStorageEditor {
    fun putString(key: String, value: String?)

    fun putInt(key: String, value: Int)

    fun putBoolean(key: String, value: Boolean)
}

class SharedPreferencesStorage(
    private val sharedPreferences: SharedPreferences
) : PreferencesStorage {

    override fun getString(key: String, defaultValue: String?): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun edit(block: PreferencesStorageEditor.() -> Unit) {
        val editor = SharedPreferencesEditor(sharedPreferences.edit())
        editor.block()
        editor.apply()
    }

    private class SharedPreferencesEditor(
        private val editor: SharedPreferences.Editor
    ) : PreferencesStorageEditor {

        override fun putString(key: String, value: String?) {
            editor.putString(key, value)
        }

        override fun putInt(key: String, value: Int) {
            editor.putInt(key, value)
        }

        override fun putBoolean(key: String, value: Boolean) {
            editor.putBoolean(key, value)
        }

        fun apply() {
            editor.apply()
        }
    }
}
