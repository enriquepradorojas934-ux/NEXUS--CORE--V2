package com.example.util

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Proveedor seguro y tolerante a fallos para componentes de Firebase.
 * Garantiza que FirebaseApp esté inicializado antes de obtener instancias de FirebaseAuth o Firestore,
 * incluso si no existe google-services.json en el entorno de desarrollo.
 */
object FirebaseProvider {
    private const val TAG = "FirebaseProvider"

    @Synchronized
    fun ensureFirebaseInitialized(context: Context?): FirebaseApp? {
        val existing = FirebaseApp.getApps(context ?: return null)
        if (existing.isNotEmpty()) {
            return FirebaseApp.getInstance()
        }

        // Intento 1: Inicialización estándar con recursos o google-services.json
        try {
            val app = FirebaseApp.initializeApp(context)
            if (app != null) {
                Log.d(TAG, "FirebaseApp inicializado con éxito usando Context.")
                return app
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp.initializeApp(context) falló: ${e.message}. Probando fallback seguro con FirebaseOptions.")
        }

        // Intento 2: Fallback con FirebaseOptions predeterminadas para entorno sin google-services.json
        return try {
            val options = FirebaseOptions.Builder()
                .setApplicationId(context.packageName)
                .setProjectId("lifeos-personal-executive")
                .setApiKey("AIzaSyFallbackKeyForLocalSandboxEnvSafe001")
                .build()
            val app = FirebaseApp.initializeApp(context, options)
            Log.d(TAG, "FirebaseApp inicializado con éxito usando FirebaseOptions de respaldo.")
            app
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo inicializar FirebaseApp: ${e.message}")
            null
        }
    }

    fun getAuth(context: Context? = null): FirebaseAuth? {
        return try {
            ensureFirebaseInitialized(context)
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth no disponible: ${e.message}")
            null
        }
    }

    fun getFirestore(context: Context? = null): FirebaseFirestore? {
        return try {
            ensureFirebaseInitialized(context)
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore no disponible: ${e.message}")
            null
        }
    }
}
