package com.example.casinoapp

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.casinoapp.notification.NotifyHelper
import com.example.casinoapp.view.HomeScreen
import com.example.casinoapp.view.LoginScreen
import com.example.casinoapp.view.SignUpScreen
import com.example.casinoapp.viewmodel.CasinoViewModel

/**
 * Activity principal de la app.
 *
 * Responsabilidades:
 * - Inicializar recursos nativos (canales de notificación).
 * - Montar el árbol Compose con `setContent`.
 * - Configurar navegación (NavHost): login → signup → home.
 * - Crear el `CasinoViewModel` (usando una Factory que recibe Application).
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Asegura los canales (Android 8+)
        //    Crea/actualiza los NotificationChannels que usará NotifyHelper.
        NotifyHelper.ensureChannels(this)

        // 2) ⚠️ Prueba rápida: envía una notificación al abrir la app
        //    Útil para demostrar el recurso nativo; puedes removerlo en producción.
        NotifyHelper.sendBonoDiarioNow(this)

        setContent {
            // Controlador de navegación Compose (stack de pantallas)
            val nav = rememberNavController()

            // Host de Snackbars compartido por toda la app
            val snack = remember { SnackbarHostState() }

            // Necesitamos el Application para el ViewModel (Room, DataStore, etc.)
            val application = LocalContext.current.applicationContext as Application

            // Factory manual para instanciar el CasinoViewModel con Application
            val casinoVm: CasinoViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return CasinoViewModel(application) as T
                    }
                }
            )

            // Estructura base de pantalla: lugar para Snackbars + contenido navegable
            Scaffold(snackbarHost = { SnackbarHost(hostState = snack) }) { _ ->

                // Gráfico de navegación con ruta inicial "login"
                NavHost(navController = nav, startDestination = "login") {

                    // ---- Pantalla de Login ----
                    composable("login") {
                        LoginScreen(
                            snackbarHostState = snack,
                            onLogin = { _, _ ->
                                // Al loguear, pedimos al VM que cargue datos del usuario
                                casinoVm.loadUserData()
                                // Navegamos a Home y sacamos Login del backstack
                                nav.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToSignUp = { nav.navigate("signup") }
                        )
                    }

                    // ---- Pantalla de Registro ----
                    composable("signup") {
                        SignUpScreen(
                            snackbarHostState = snack,
                            onSignUp = { _, _, _ -> /* flujo manejado dentro del VM */ },
                            onBackToLogin = { nav.popBackStack() } // vuelve a la anterior (login)
                        )
                    }

                    // ---- Pantalla Home (dashboard + juegos) ----
                    composable("home") {
                        HomeScreen(
                            viewModel = casinoVm,
                            snackbarHostState = snack,
                            onLogout = {
                                // Cierra sesión en el VM y vuelve a Login limpiando Home
                                casinoVm.logout()
                                nav.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
