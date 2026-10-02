package com.example.service

import android.content.Context
import com.example.data.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Servicio de Autenticación local soberano.
 * Opera en modo local sin ejecutar llamadas de red ni inicializaciones de Firebase.
 */
class FirebaseAuthService {

    /**
     * Emite null de forma estática y segura para modo local.
     */
    val currentUserFlow: Flow<AuthUser?> = flowOf(null)

    /**
     * Retorna null indicando modo local sin sesión remota.
     */
    fun getCurrentUser(): AuthUser? = null

    /**
     * Retorna el UID predeterminado para asociar datos locales.
     */
    fun getCurrentUserId(): String = "executive_user_default"

    /**
     * Desactivado en modo local soberano.
     */
    suspend fun signInWithGoogle(context: Context, serverClientId: String? = null): Result<AuthUser> {
        return Result.failure(IllegalStateException("Modo local (Room) activo."))
    }

    /**
     * Desactivado en modo local soberano.
     */
    suspend fun signInAnonymously(): Result<AuthUser> {
        return Result.failure(IllegalStateException("Modo local (Room) activo."))
    }

    /**
     * Cierre de sesión local seguro.
     */
    suspend fun signOut(context: Context? = null): Result<Unit> {
        return Result.success(Unit)
    }
}
