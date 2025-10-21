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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Asegura los canales (Android 8+)
        NotifyHelper.ensureChannels(this)

        // 2) ⚠️ Prueba rápida: envía una notificación al abrir la app
        //    (déjalo mientras pruebas; luego puedes quitarlo)
        NotifyHelper.sendBonoDiarioNow(this)

        setContent {
            val nav = rememberNavController()
            val snack = remember { SnackbarHostState() }
            val application = LocalContext.current.applicationContext as Application

            val casinoVm: CasinoViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return CasinoViewModel(application) as T
                    }
                }
            )

            Scaffold(snackbarHost = { SnackbarHost(hostState = snack) }) { _ ->
                NavHost(navController = nav, startDestination = "login") {

                    composable("login") {
                        LoginScreen(
                            snackbarHostState = snack,
                            onLogin = { _, _ ->
                                casinoVm.loadUserData()
                                nav.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToSignUp = { nav.navigate("signup") }
                        )
                    }

                    composable("signup") {
                        SignUpScreen(
                            snackbarHostState = snack,
                            onSignUp = { _, _, _ -> },
                            onBackToLogin = { nav.popBackStack() }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            viewModel = casinoVm,
                            snackbarHostState = snack,
                            onLogout = {
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
