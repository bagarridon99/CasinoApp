package com.example.casinoapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.casinoapp.ui.theme.CasinoAppTheme
import com.example.casinoapp.view.HomeScreen
import com.example.casinoapp.view.LoginScreen
import com.example.casinoapp.view.SignUpScreen
import com.example.casinoapp.viewmodel.AuthViewModel
import com.example.casinoapp.viewmodel.CasinoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CasinoAppTheme { CasinoApp() }
        }
    }
}

@Composable
fun CasinoApp(viewModel: CasinoViewModel = viewModel()) {
    // Estado del juego existente
    val uiState by remember { derivedStateOf { viewModel.uiState } }
    val snackbarHostState = remember { SnackbarHostState() }
    val showSignUpState = remember { mutableStateOf(false) }

    // AuthViewModel con Application
    val app = LocalContext.current.applicationContext as Application
    val authVm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(app) as T
        }
    })
    val authState by authVm.state.collectAsState(initial = null)

    // Restaurar sesión al iniciar
    LaunchedEffect(Unit) { authVm.loadSession() }

    // Mensajes de tu VM de juego
    LaunchedEffect(uiState.statusMessage) {
        val msg = uiState.statusMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeMessage()
        }
    }

    // Mensajes de auth (explícito para evitar el 'it')
    LaunchedEffect(authState?.msg) {
        val msg = authState?.msg
        if (msg != null) snackbarHostState.showSnackbar(msg)
    }

    val isLoggedIn = (authState?.email != null)

    if (!isLoggedIn) {
        if (showSignUpState.value) {
            SignUpScreen(
                snackbarHostState = snackbarHostState,
                onSignUp = { _, _, _ -> showSignUpState.value = false },
                onBackToLogin = { showSignUpState.value = false }
            )
        } else {
            LoginScreen(
                snackbarHostState = snackbarHostState,
                onLogin = { _, _ -> /* navegación la maneja authState */ },
                onNavigateToSignUp = { showSignUpState.value = true }
            )
        }
    } else {
        HomeScreen(
            viewModel = viewModel,
            snackbarHostState = snackbarHostState,
            onLogout = { authVm.logout() } // usa tu botón "Salir" existente
        )
    }
}
