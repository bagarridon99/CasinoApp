package com.example.casinoapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
        NotifyHelper.ensureChannels(this)

        setContent {
            val nav = rememberNavController()
            val snack = remember { SnackbarHostState() }
            val application = LocalContext.current.applicationContext as Application

            // Proveedor de ViewModel corregido
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

                    // PANTALLA LOGIN
                    composable(
                        "login",
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500)) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500)) }
                    ) {
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

                    // PANTALLA REGISTRO
                    composable(
                        "signup",
                        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500)) },
                        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500)) }
                    ) {
                        SignUpScreen(
                            snackbarHostState = snack,
                            // Simplificamos la acción: SignUpScreen se encarga de su lógica
                            // y MainActivity solo escucha cuándo navegar.
                            onSignUp = { user, email, pass ->
                                // Navegación inmediata a Home
                                nav.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                                // Opcional: Cargar datos iniciales
                                casinoVm.loadUserData()
                            },
                            onBackToLogin = { nav.popBackStack() }
                        )
                    }

                    // PANTALLA HOME
                    composable(
                        "home",
                        enterTransition = { fadeIn(tween(700)) + scaleIn(initialScale = 0.95f, animationSpec = tween(700)) },
                        exitTransition = { fadeOut(tween(500)) + scaleOut(targetScale = 0.95f, animationSpec = tween(500)) }
                    ) {
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