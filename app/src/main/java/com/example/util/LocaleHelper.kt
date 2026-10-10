package com.example.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    const val SYSTEM_DEFAULT = "Idioma del sistema"

    fun getLocaleForLanguage(language: String): Locale {
        val lower = language.lowercase().trim()
        return when {
            lower.contains("sistema") || lower.contains("system") || lower.contains("default") || lower.isEmpty() -> {
                val sys = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Resources.getSystem().configuration.locales[0] ?: Locale.getDefault()
                    } else {
                        @Suppress("DEPRECATION")
                        Resources.getSystem().configuration.locale ?: Locale.getDefault()
                    }
                } catch (_: Exception) {
                    Locale.getDefault()
                }

                if (sys.language.startsWith("es")) {
                    Locale("es", "419")
                } else if (sys.language.startsWith("en")) {
                    Locale("en", "US")
                } else {
                    Locale("en", "US") // Respaldo estándar en inglés
                }
            }
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
            else -> Locale("en", "US")
        }
    }

    fun isSpanish(language: String): Boolean {
        return getLocaleForLanguage(language).language.startsWith("es")
    }

    fun getLanguageCode(language: String): String {
        return getLocaleForLanguage(language).language
    }

    fun applyLocale(context: Context, language: String): Context {
        val locale = getLocaleForLanguage(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        }

        // Si es Android 13+ y se puede invocar LocaleManager, sincronizar a nivel de sistema para la app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                val targetList = if (language.contains("sistema", ignoreCase = true) || language.contains("system", ignoreCase = true)) {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList(locale)
                }
                localeManager?.applicationLocales = targetList
            } catch (_: Exception) {}
        }

        return context.createConfigurationContext(config)
    }
}
