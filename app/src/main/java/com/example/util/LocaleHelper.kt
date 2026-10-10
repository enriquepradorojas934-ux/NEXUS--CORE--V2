package com.example.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    const val SYSTEM_DEFAULT = "system"

    fun isSpanish(language: String): Boolean {
        if (language.equals(SYSTEM_DEFAULT, ignoreCase = true)) {
            val def = Locale.getDefault().language
            return def.startsWith("es", ignoreCase = true)
        }
        return language.startsWith("es", ignoreCase = true) || language.contains("español", ignoreCase = true) || language.contains("spanish", ignoreCase = true)
    }

    fun getLocaleForLanguage(language: String): Locale {
        return if (language.equals(SYSTEM_DEFAULT, ignoreCase = true)) {
            Locale.getDefault()
        } else if (isSpanish(language)) {
            Locale("es")
        } else {
            Locale("en")
        }
    }

    fun applyLocale(context: Context, language: String): Context {
        val locale = getLocaleForLanguage(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
