package com.example.ochataku.manager

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {

    fun getCurrentLanguage(context: Context): String {
        val locale =
            context.resources.configuration.locales[0]
        return locale.language
    }

    fun switchLanguage(activity: Activity, lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        activity.recreate()
    }

}
