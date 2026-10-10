package com.example.util

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseProvider {
    fun getFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        }
    }
}
