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
        com.example.util.FirebaseProvider.ensureFirebaseInitialized(this)
        initFirebaseAppCheck()
    }

    /**
     * Inicializa Firebase App Check con el proveedor reCAPTCHA.
     * En modo depuración, permite el uso de DebugAppCheckProviderFactory para pruebas locales,
     * y en producción instala RecaptchaAppCheckProviderFactory con la site key.
     */
    private fun initFirebaseAppCheck() {
        try {
            // Asegurar que FirebaseApp esté inicializado en el contexto de la aplicación
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }

            val appCheck = FirebaseAppCheck.getInstance()

            // Clave de sitio de reCAPTCHA configurada en Firebase Console / Google Cloud
            val siteKey = BuildConfig.RECAPTCHA_SITE_KEY.ifBlank {
                DEFAULT_RECAPTCHA_SITE_KEY
            }

            // Inicialización de Firebase App Check con el proveedor reCAPTCHA
            // para autenticar y proteger el backend y las llamadas a la API contra tráfico fraudulento.
            val providerFactory = if (BuildConfig.DEBUG) {
                // En modo debug, DebugAppCheckProviderFactory permite pruebas locales en emulador/CI
                // generando un token de prueba registrado en Firebase Console.
                DebugAppCheckProviderFactory.getInstance()
            } else {
                // En producción, reCAPTCHA evalúa el riesgo invisiblemente
                RecaptchaAppCheckProviderFactory.getInstance(siteKey)
            }

            appCheck.installAppCheckProviderFactory(providerFactory)
            Log.d(TAG, "Firebase App Check inicializado correctamente con reCAPTCHA.")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase App Check no se pudo inicializar en este entorno: ${e.message}")
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
