package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

/**
 * Clase Application principal del Sistema Operativo Personal.
 * Configura la inicialización de Firebase y Firebase App Check con reCAPTCHA
 * para asegurar las llamadas a los servicios y APIs (como Gemini / Firebase AI).
 */
class PersonalOsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            com.example.util.FirebaseProvider.ensureFirebaseInitialized(this)
            initFirebaseAppCheck()
        } catch (t: Throwable) {
            Log.w(TAG, "Inicialización segura de Firebase en arranque: ${t.message}")
        }
    }

    /**
     * Inicializa Firebase App Check de forma segura si las credenciales están disponibles.
     */
    private fun initFirebaseAppCheck() {
        try {
            // Solo intentar si hay clave de reCAPTCHA configurada o en modo debug explícito
            val siteKey = BuildConfig.RECAPTCHA_SITE_KEY
            if (siteKey.isBlank()) {
                Log.d(TAG, "App Check omitido: RECAPTCHA_SITE_KEY no configurada.")
                return
            }

            if (FirebaseApp.getApps(this).isEmpty()) {
                com.example.util.FirebaseProvider.ensureFirebaseInitialized(this)
            }

            if (FirebaseApp.getApps(this).isNotEmpty()) {
                val appCheck = FirebaseAppCheck.getInstance()
                val providerFactory = if (BuildConfig.DEBUG) {
                    DebugAppCheckProviderFactory.getInstance()
                } else {
                    RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                }
                appCheck.installAppCheckProviderFactory(providerFactory)
                Log.d(TAG, "Firebase App Check configurado.")
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Firebase App Check no inicializado (entorno local): ${t.message}")
        }
    }

    /**
     * Permite instalar explícitamente el proveedor reCAPTCHA con una clave específica.
     */
    fun installRecaptchaAppCheck(siteKey: String = BuildConfig.RECAPTCHA_SITE_KEY) {
        try {
            val key = siteKey.ifBlank { DEFAULT_RECAPTCHA_SITE_KEY }
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.installAppCheckProviderFactory(
                RecaptchaAppCheckProviderFactory.getInstance(key)
            )
            Log.d(TAG, "Instalado explícitamente RecaptchaAppCheckProviderFactory con clave.")
        } catch (e: Exception) {
            Log.w(TAG, "Error instalando RecaptchaAppCheckProviderFactory: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "PersonalOsApplication"
        // Clave pública de prueba reCAPTCHA estándar de Google
        const val DEFAULT_RECAPTCHA_SITE_KEY = "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI"
    }
}
