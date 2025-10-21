package com.example.casinoapp.notification

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.max

object ReminderScheduler {

    private const val UNIQUE_DAILY_BONO = "daily_bono_reminder"

    fun scheduleDailyBonoAt(context: Context, hour24: Int = 9, minute: Int = 0) {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val delay = max(0, next.timeInMillis - now.timeInMillis)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = OneTimeWorkRequestBuilder<DailyBonoWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_DAILY_BONO,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
