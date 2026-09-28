package com.example.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun getLocaleForLanguage(language: String): Locale {
        val lower = language.lowercase().trim()
        return when {
            lower.contains("español") || lower.contains("espanol") -> {
                if (lower.contains("españa") || lower.contains("espana")) {
                    Locale("es", "ES")
                } else {
                    Locale("es", "419") // Español Latinoamericano
                }
            }
            lower.contains("english") || lower.contains("inglés") || lower.contains("ingles") -> {
                if (lower.contains("kingdom") || lower.contains("uk") || lower.contains("britain")) {
                    Locale("en", "GB")
                } else {
                    Locale("en", "US")
                }
            }
            lower.contains("português") || lower.contains("portugues") -> {
                if (lower.contains("portugal")) {
                    Locale("pt", "PT")
                } else {
                    Locale("pt", "BR")
                }
            }
            lower.contains("français") || lower.contains("francais") || lower.contains("french") -> Locale("fr")
            lower.contains("deutsch") || lower.contains("german") -> Locale("de")
            lower.contains("italiano") || lower.contains("italian") -> Locale("it")
            else -> Locale("en", "US") // Fallback standard en inglés
        }
    }

    fun getLanguageCode(language: String): String {
        return getLocaleForLanguage(language).language
    }

    fun applyLocale(context: Context, language: String): Context {
        val locale = getLocaleForLanguage(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
