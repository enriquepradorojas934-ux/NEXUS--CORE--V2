package com.example.util

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Proveedor local soberano: Desactivado para garantizar operación 100% offline con Room.
 */
object FirebaseProvider {
    fun ensureFirebaseInitialized(context: Context?): FirebaseApp? = null
    fun getAuth(context: Context? = null): FirebaseAuth? = null
    fun getFirestore(context: Context? = null): FirebaseFirestore? = null
}

