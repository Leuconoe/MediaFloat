package com.mediacontrol.floatingwidget.model

enum class AppLanguage(
    val storageValue: String,
    val languageTag: String
) {
    SystemDefault(storageValue = "system", languageTag = ""),
    English(storageValue = "en", languageTag = "en"),
    Korean(storageValue = "ko", languageTag = "ko"),
    Chinese(storageValue = "zh", languageTag = "zh"),
    Japanese(storageValue = "ja", languageTag = "ja"),
    Spanish(storageValue = "es", languageTag = "es"),
    French(storageValue = "fr", languageTag = "fr");

    companion object {
        fun fromStorageValue(value: String?): AppLanguage {
            return entries.firstOrNull { it.storageValue == value } ?: SystemDefault
        }
    }
}

data class AppPreferences(
    val debugToolsEnabled: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.SystemDefault
)
