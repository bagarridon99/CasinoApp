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

/**
 * Utilidad centralizada para:
 * - Crear canales de notificación (Android 8+).
 * - Construir y enviar notificaciones para distintos casos (bono diario, racha, bono por ciudad).
 * - Generar PendingIntent que abre la app con una "ruta" (nav_target) específica.
 */
object NotifyHelper {

    private const val TAG = "NotifyHelper"

    // IDs de canales para agrupar notificaciones
    const val CH_BONUS = "ch_bonus"
    const val CH_GENERAL = "ch_general"

    /**
     * Crea/asegura los canales de notificación requeridos (Android 8+).
     * - CH_BONUS: para bonos y recompensas.
     * - CH_GENERAL: para avisos generales (progreso, rachas).
     */
    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canal de Bonos
        val bonus = NotificationChannel(
            CH_BONUS,
            "Bonos y Recompensas",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Recordatorios de bonos (diario, por ciudad, etc.)"
            enableLights(true); lightColor = Color.GREEN
            enableVibration(true)
        }

        // Canal General
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

    /**
     * Construye un PendingIntent que abre MainActivity con una "ruta" (navTarget)
     * para que la UI navegue a la sección correspondiente (ej: "BONO_DIARIO").
     */
    private fun pendingToHome(
        context: Context,
        navTarget: String,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("nav_target", navTarget) // pista para la navegación interna
        }

        // TaskStackBuilder asegura la back stack correcta al abrir desde notificación
        val builder = TaskStackBuilder.create(context).apply {
            addNextIntentWithParentStack(intent)
        }

        // FLAG_IMMUTABLE en 23+ por seguridad; UPDATE_CURRENT para reutilizar el mismo PI
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

    /**
     * Envía una notificación de "Bono diario".
     * - Canal: CH_BONUS
     * - Acción: abre la app en la sección para reclamar el bono.
     */
    fun sendBonoDiarioNow(context: Context) {
        ensureChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val goHome = pendingToHome(context, "BONO_DIARIO", 1001)

        // Acción secundaria en la notificación: botón "Ir a reclamar"
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

        // Construcción de la notificación
        val notif = NotificationCompat.Builder(context, CH_BONUS)
            .setSmallIcon(R.drawable.ic_stat_casino)      // icono del proyecto
            .setContentTitle("🎁 Bono diario listo")       // título visible
            .setContentText("Tienes +20% en tu próximo depósito. ¡Reclámalo ahora!")
            .setContentIntent(goHome)                      // abre app al tocar la notificación
            .setAutoCancel(true)                           // se cierra al tocar
            .addAction(0, "Ir a reclamar", action)         // acción de botón
            .build()

        Log.d(TAG, "Notificando BONO_DIARIO (id=2001)")
        nm.notify(2001, notif) // ID único para esta notificación
    }

    /**
     * Notificación para recordar una racha activa de partidas.
     * - Canal: CH_GENERAL
     */
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

    /**
     * Notificación para bono por ciudad detectada (geolocalización).
     * - Canal: CH_BONUS
     */
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
