package com.example.service

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.BuildConfig
import com.example.data.model.AuthUser
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Servicio de Autenticación con Firebase Auth y Credential Manager (Google Sign-In).
 * Permite:
 * 1. Flujo seguro de inicio de sesión con Google vía Credential Manager API moderna.
 * 2. Autenticación con usuario anónimo para modo local o de prueba.
 * 3. Cierre de sesión y desvinculación.
 * 4. Flujo reactivo del usuario autenticado actual.
 */
class FirebaseAuthService(
    private val authProvider: () -> FirebaseAuth? = { com.example.util.FirebaseProvider.getAuth() }
) {
    companion object {
        private const val TAG = "FirebaseAuthService"
    }

    private val auth: FirebaseAuth?
        get() = authProvider()

    /**
     * Emite los cambios en el estado de autenticación del usuario actual en tiempo real.
     */
    val currentUserFlow: Flow<AuthUser?> = callbackFlow {
        val currentAuth = auth
        if (currentAuth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser?.toAuthUser()
            trySend(user)
        }
        currentAuth.addAuthStateListener(listener)
        // Emitir estado inicial
        trySend(currentAuth.currentUser?.toAuthUser())

        awaitClose {
            currentAuth.removeAuthStateListener(listener)
        }
    }

    /**
     * Retorna el usuario autenticado actual o null si no hay sesión activa.
     */
    fun getCurrentUser(): AuthUser? = auth?.currentUser?.toAuthUser()

    /**
     * Retorna el UID del usuario actual para asociar sus datos en Cloud Firestore,
     * o un ID predeterminado si no ha iniciado sesión.
     */
    fun getCurrentUserId(): String = auth?.currentUser?.uid ?: "executive_user_default"

    /**
     * Inicia sesión con Google utilizando Credential Manager y GoogleIdTokenCredential.
     * Si no se ha configurado un Web Client ID específico en BuildConfig o Secrets,
     * se informa claramente al usuario para que ingrese su Web Client ID en el panel de Secrets.
     */
    suspend fun signInWithGoogle(context: Context, serverClientId: String? = null): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val webClientId = serverClientId?.takeIf { it.isNotBlank() }
                ?: getWebClientId(context)

            if (webClientId.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException(
                        "Para habilitar Google Sign-In, configure su Web Client ID de OAuth 2.0 (GOOGLE_WEB_CLIENT_ID) en el panel de Secretos de AI Studio."
                    )
                )
            }

            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = try {
                credentialManager.getCredential(
                    request = request,
                    context = context
                )
            } catch (e: GetCredentialCancellationException) {
                return@withContext Result.failure(Exception("Inicio de sesión con Google cancelado por el usuario."))
            } catch (e: GetCredentialException) {
                return@withContext Result.failure(Exception("Error de Credential Manager: ${e.message}"))
            }

            val currentAuth = auth ?: return@withContext Result.failure(
                IllegalStateException("El servicio de Firebase Auth no está inicializado.")
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = currentAuth.signInWithCredential(firebaseCredential).await()
                    val firebaseUser = authResult.user
                        ?: return@withContext Result.failure(Exception("No se pudo obtener el usuario de Firebase"))
                    
                    Log.i(TAG, "Inicio de sesión exitoso con Google: ${firebaseUser.email}")
                    Result.success(firebaseUser.toAuthUser())
                } catch (e: GoogleIdTokenParsingException) {
                    Result.failure(Exception("Error al procesar credencial de Google: ${e.message}"))
                }
            } else {
                Result.failure(Exception("Tipo de credencial no soportado: ${credential.type}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error durante signInWithGoogle", e)
            Result.failure(e)
        }
    }

    /**
     * Inicia sesión anónima en Firebase Auth para sincronizar en Firestore
     * sin requerir una cuenta de Google previa.
     */
    suspend fun signInAnonymously(): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val currentAuth = auth ?: return@withContext Result.failure(
                IllegalStateException("El servicio de Firebase Auth no está inicializado.")
            )
            val authResult = currentAuth.signInAnonymously().await()
            val user = authResult.user
                ?: return@withContext Result.failure(Exception("Error al generar sesión anónima"))
            Log.i(TAG, "Sesión anónima iniciada con UID: ${user.uid}")
            Result.success(user.toAuthUser())
        } catch (e: Exception) {
            Log.e(TAG, "Error en signInAnonymously", e)
            Result.failure(e)
        }
    }

    /**
     * Cierra la sesión activa en Firebase Auth y desvincula las credenciales locales.
     */
    suspend fun signOut(context: Context? = null): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            auth?.signOut()
            context?.let { ctx ->
                try {
                    val credentialManager = CredentialManager.create(ctx)
                    credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo limpiar el estado de CredentialManager", e)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Intenta resolver el Client ID de OAuth desde strings.xml o variables de configuración.
     */
    private fun getWebClientId(context: Context): String {
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isAnonymous = isAnonymous
        )
    }
}
