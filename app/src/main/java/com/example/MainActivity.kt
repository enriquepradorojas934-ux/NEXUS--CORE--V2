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
        FirebaseProvider.ensureFirebaseInitialized(applicationContext)
        enableEdgeToEdge()
        setContent {
            PersonalOsApp()
        }
    }
}
