package com.example.casinoapp.notification

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Programador de recordatorios usando WorkManager.
 * - scheduleDailyBonoAt: agenda el "Bono Diario" para una hora/minuto específicos
 *   (reemplaza cualquier trabajo previo con el mismo nombre único).
 */
object ReminderScheduler {

    private const val UNIQUE_DAILY_BONO = "daily_bono_reminder" // nombre único del work

    /**
     * Agenda un OneTimeWorkRequest para ejecutar DailyBonoWorker a la hora indicada.
     * Si la hora ya pasó hoy, lo programa para mañana.
     * - Usa constraints básicos (no requiere red).
     * - ExistingWorkPolicy.REPLACE para actualizar la programación si se re-llama.
     */
    fun scheduleDailyBonoAt(context: Context, hour24: Int = 9, minute: Int = 0) {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1) // si ya pasó, mueve a mañana
        }
        val delay = max(0, next.timeInMillis - now.timeInMillis) // ms hasta la ejecución

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // no necesita red
            .build()

        val request = OneTimeWorkRequestBuilder<DailyBonoWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS) // programa para la hora objetivo
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_DAILY_BONO,
            ExistingWorkPolicy.REPLACE, // reemplaza si ya había uno
            request
        )
    }
}
