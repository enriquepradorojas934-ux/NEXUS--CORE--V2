package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.PersonalOsApp
import com.example.ui.theme.MyApplicationTheme
import com.example.util.FirebaseProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            FirebaseProvider.ensureFirebaseInitialized(applicationContext)
        } catch (_: Throwable) {
            // Continuar en modo soberano local sin interrumpir el arranque
        }
        enableEdgeToEdge()
        setContent {
            PersonalOsApp()
        }
    }
}
