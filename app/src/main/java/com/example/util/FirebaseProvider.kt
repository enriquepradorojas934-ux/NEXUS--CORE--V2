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
        val ctx = context ?: return null
        return try {
            val existing = FirebaseApp.getApps(ctx)
            if (existing.isNotEmpty()) {
                try {
                    return FirebaseApp.getInstance()
                } catch (_: Throwable) {
                    return existing.firstOrNull()
                }
            }

            // Intento 1: Inicialización estándar con Context
            try {
                val app = FirebaseApp.initializeApp(ctx)
                if (app != null) return app
            } catch (e: Throwable) {
                Log.w(TAG, "Inicialización estándar de Firebase no disponible: ${e.message}")
            }

            // Intento 2: Fallback con FirebaseOptions predeterminadas para entorno sin google-services.json real
            try {
                val options = FirebaseOptions.Builder()
                    .setApplicationId(ctx.packageName)
                    .setProjectId("lifeos-personal-executive")
                    .setApiKey("AIzaSyFallbackKeyForLocalSandboxEnvSafe001")
                    .build()
                FirebaseApp.initializeApp(ctx, options)
            } catch (e: Throwable) {
                Log.d(TAG, "Modo local soberano activo sin Firebase: ${e.message}")
                null
            }
        } catch (t: Throwable) {
            Log.w(TAG, "No se pudo inicializar FirebaseApp: ${t.message}")
            null
        }
    }

    fun getAuth(context: Context? = null): FirebaseAuth? {
        return try {
            ensureFirebaseInitialized(context)
            FirebaseAuth.getInstance()
        } catch (t: Throwable) {
            Log.w(TAG, "FirebaseAuth no disponible: ${t.message}")
            null
        }
    }

    fun getFirestore(context: Context? = null): FirebaseFirestore? {
        return try {
            ensureFirebaseInitialized(context)
            FirebaseFirestore.getInstance()
        } catch (t: Throwable) {
            Log.w(TAG, "FirebaseFirestore no disponible: ${t.message}")
            null
        }
    }
}
