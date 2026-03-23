package com.mediacontrol.floatingwidget

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.mediacontrol.floatingwidget.model.AppLanguage

object AppLocaleManager {

    fun apply(appLanguage: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(localeListFor(appLanguage))
    }

    private fun localeListFor(appLanguage: AppLanguage): LocaleListCompat {
        return if (appLanguage == AppLanguage.SystemDefault) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(appLanguage.languageTag)
        }
    }
}
