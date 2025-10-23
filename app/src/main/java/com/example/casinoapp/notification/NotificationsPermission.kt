package com.example.casinoapp.notification

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Composable que solicita el permiso de notificaciones UNA sola vez (Android 13+, API 33).
 * - En versiones < 33 no hace nada (no se requiere POST_NOTIFICATIONS).
 * - Usa rememberLauncherForActivityResult para lanzar el diálogo del sistema.
 * - Controla un flag local 'asked' para evitar pedirlo repetidamente.
 */
@Composable
fun AskNotificationsPermissionOnce() {
    if (Build.VERSION.SDK_INT < 33) return // en Android 12 o menos no se pide

    val context = LocalContext.current
    var asked by remember { mutableStateOf(false) }

    // Launcher para solicitar el permiso POST_NOTIFICATIONS
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted/denied -> podrías persistir el resultado si lo necesitas */ }

    // Efecto lanzado al entrar en composición
    LaunchedEffect(Unit) {
        if (!asked) {
            asked = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
