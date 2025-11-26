package com.example.casinoapp

import android.app.Application

class CasinoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Si más adelante quieres inicializar cosas globales (WorkManager, notificaciones, etc.)
        // este es el lugar para hacerlo.
    }
}
