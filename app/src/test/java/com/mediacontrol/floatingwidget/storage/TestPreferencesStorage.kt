package sw2.io.mediafloat.storage

class TestPreferencesStorage : PreferencesStorage {
    private val values = linkedMapOf<String, Any?>()

    override fun getString(key: String, defaultValue: String?): String? {
        return values[key] as? String ?: defaultValue
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return values[key] as? Int ?: defaultValue
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return values[key] as? Boolean ?: defaultValue
    }

    override fun edit(block: PreferencesStorageEditor.() -> Unit) {
        val editor = Editor(values)
        editor.block()
    }

    private class Editor(
        private val values: MutableMap<String, Any?>
    ) : PreferencesStorageEditor {

        override fun putString(key: String, value: String?) {
            values[key] = value
        }

        override fun putInt(key: String, value: Int) {
            values[key] = value
        }

        override fun putBoolean(key: String, value: Boolean) {
            values[key] = value
        }
    }
}
