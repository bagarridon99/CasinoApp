package com.example.casinoapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.casinoapp.MainActivity
import com.example.casinoapp.R

object NotifyHelper {

    private const val TAG = "NotifyHelper"

    const val CH_BONUS = "ch_bonus"
    const val CH_GENERAL = "ch_general"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val bonus = NotificationChannel(
            CH_BONUS,
            "Bonos y Recompensas",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Recordatorios de bonos (diario, por ciudad, etc.)"
            enableLights(true); lightColor = Color.GREEN
            enableVibration(true)
        }

        val general = NotificationChannel(
            CH_GENERAL,
            "General",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Progreso, rachas y noticias"
            enableLights(true); lightColor = Color.CYAN
            enableVibration(true)
        }

        nm.createNotificationChannel(bonus)
        nm.createNotificationChannel(general)
    }

    /** PendingIntent que abre MainActivity con un destino interno */
    private fun pendingToHome(
        context: Context,
        navTarget: String,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("nav_target", navTarget)
        }

        val builder = TaskStackBuilder.create(context).apply {
            addNextIntentWithParentStack(intent)
        }

        return if (Build.VERSION.SDK_INT >= 23) {
            builder.getPendingIntent(
                requestCode,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )!!
        } else {
            builder.getPendingIntent(
                requestCode,
                PendingIntent.FLAG_UPDATE_CURRENT
            )!!
        }
    }

    /** 1) 🎁 Bono diario listo (+20%) — envío inmediato */
    fun sendBonoDiarioNow(context: Context) {
        ensureChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val goHome = pendingToHome(context, "BONO_DIARIO", 1001)

        val action = if (Build.VERSION.SDK_INT >= 23)
            PendingIntent.getActivity(
                context, 1002,
                Intent(context, MainActivity::class.java).putExtra("nav_target", "BONO_DIARIO"),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        else
            PendingIntent.getActivity(
                context, 1002,
                Intent(context, MainActivity::class.java).putExtra("nav_target", "BONO_DIARIO"),
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val notif = NotificationCompat.Builder(context, CH_BONUS)
            // usa un icono seguro del proyecto; si tu vector propio falla, usa ic_dialog_info del sistema
            .setSmallIcon(R.drawable.ic_stat_casino)
            .setContentTitle("🎁 Bono diario listo")
            .setContentText("Tienes +20% en tu próximo depósito. ¡Reclámalo ahora!")
            .setContentIntent(goHome)             // admite non-null
            .setAutoCancel(true)
            .addAction(0, "Ir a reclamar", action) // action requiere non-null
            .build()

        Log.d(TAG, "Notificando BONO_DIARIO (id=2001)")
        nm.notify(2001, notif)
    }

    /** 2) 🔥 Racha activa — por ejemplo: “te faltan N partidas” */
    fun sendRachaNow(context: Context, restantes: Int) {
        ensureChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pi = pendingToHome(context, "RACHA", 1003)

        val notif = NotificationCompat.Builder(context, CH_GENERAL)
            .setSmallIcon(R.drawable.ic_stat_casino)
            .setContentTitle("🔥 Racha activa")
            .setContentText("Te faltan $restantes partidas para subir de nivel.")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        Log.d(TAG, "Notificando RACHA (id=2002)")
        nm.notify(2002, notif)
    }

    /** 3) 📍 Bono por ciudad */
    fun sendCityBonusNow(context: Context, city: String) {
        ensureChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pi = pendingToHome(context, "BONO_CIUDAD", 1004)

        val notif = NotificationCompat.Builder(context, CH_BONUS)
            .setSmallIcon(R.drawable.ic_stat_casino)
            .setContentTitle("📍 Bono por ciudad")
            .setContentText("Disponible en $city. Obtén +20% en tu próximo depósito.")
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        Log.d(TAG, "Notificando BONO_CIUDAD (id=2003)")
        nm.notify(2003, notif)
    }
}
