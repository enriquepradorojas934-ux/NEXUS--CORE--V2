package com.example

import android.app.Application

/**
 * Clase Application principal del Sistema Operativo Personal.
 * Opera en modo local soberano (offline-first) con Room Database.
 */
class PersonalOsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Modo local soberano Room: sin inicializaciones remotas ni dependencias de arranque
    }
}

