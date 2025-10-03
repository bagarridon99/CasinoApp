package com.example.casinoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.casinoapp.model.CasinoUiState
import com.example.casinoapp.model.RouletteColor
import com.example.casinoapp.ui.theme.CasinoAppTheme
import com.example.casinoapp.view.*
import com.example.casinoapp.viewmodel.CasinoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CasinoAppTheme {
                CasinoApp()
            }
        }
    }
}

@Composable
fun CasinoApp(viewModel: CasinoViewModel = viewModel()) {
    val uiState: CasinoUiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }
    var showSignUp by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeMessage()
        }
    }

    if (!uiState.isLoggedIn) {
        if (showSignUp) {
            SignUpScreen(
                snackbarHostState = snackbarHostState,
                onSignUp = { _, _, _ -> showSignUp = false },
                onBackToLogin = { showSignUp = false }
            )
        } else {
            LoginScreen(
                snackbarHostState = snackbarHostState,
                onLogin = { u, p -> viewModel.login(u, p) },
                onNavigateToSignUp = { showSignUp = true }
            )
        }
    } else {
        HomeScreen(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onLogout = { viewModel.logout() },
            onPlayRoulette = { bet, color: RouletteColor -> viewModel.playRoulette(bet, color) },
            onPlayBlackjack = { bet -> viewModel.playBlackjack(bet) },
            onPlaySlots = { bet -> viewModel.playSlots(bet) },
            onDeposit = { viewModel.deposit(it) },
            onWithdraw = { viewModel.withdraw(it) }
        )
    }
}
