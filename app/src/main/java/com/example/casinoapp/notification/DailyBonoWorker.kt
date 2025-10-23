package com.example.casinoapp.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Worker que dispara una notificación de "Bono Diario".
 * - Es un CoroutineWorker: puede ejecutar código suspend (corrutinas).
 * - Se invoca mediante WorkManager (programado desde ReminderScheduler).
 */
class DailyBonoWorker(appContext: Context, params: WorkerParameters)
    : CoroutineWorker(appContext, params) {

    /**
     * Punto de entrada del trabajo en segundo plano.
     * 1) Asegura que existan los canales de notificación.
     * 2) Envía la notificación del bono diario inmediatamente.
     * 3) Retorna Result.success() para indicar éxito.
     */
    override suspend fun doWork(): Result {
        NotifyHelper.ensureChannels(applicationContext)    // crea canales si faltan
        NotifyHelper.sendBonoDiarioNow(applicationContext) // notifica bono diario
        return Result.success()
    }
}
