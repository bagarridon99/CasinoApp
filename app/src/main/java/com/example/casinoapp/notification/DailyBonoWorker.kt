package com.example.casinoapp.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DailyBonoWorker(appContext: Context, params: WorkerParameters)
    : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        NotifyHelper.ensureChannels(applicationContext)
        NotifyHelper.sendBonoDiarioNow(applicationContext)
        return Result.success()
    }
}
