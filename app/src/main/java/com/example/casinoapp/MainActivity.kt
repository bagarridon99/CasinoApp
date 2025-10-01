package com.example.casinoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.casinoapp.ui.theme.CasinoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CasinoAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CasinoApp()
                }
            }
        }
    }
}

@Composable
fun CasinoApp(viewModel: CasinoViewModel = viewModel()) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    if (!uiState.isLoggedIn) {
        LoginScreen(
            snackbarHostState = snackbarHostState,
            onLogin = { user, pass -> viewModel.login(user, pass) }
        )
    } else {
        CasinoHomeScreen(
            uiState = uiState,
            snackbarHostState = snackbarHostState,
            onLogout = { viewModel.logout() },
            onPlayRoulette = { bet, color -> viewModel.playRoulette(bet, color) },
            onPlayBlackjack = { bet -> viewModel.playBlackjack(bet) },
            onPlaySlots = { bet -> viewModel.playSlots(bet) }
        )
    }
}

@Composable
fun LoginScreen(
    snackbarHostState: SnackbarHostState,
    onLogin: (String, String) -> Unit
) {
    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Casino Royale",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            val username = rememberSaveable { mutableStateOf("") }
            val password = rememberSaveable { mutableStateOf("") }

            OutlinedTextField(
                value = username.value,
                onValueChange = { username.value = it },
                label = { Text("Usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onLogin(username.value, password.value) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ingresar")
            }
        }
    }
}

private enum class HomeTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Dashboard("Inicio", Icons.Filled.Home),
    Roulette("Ruleta", Icons.Filled.Casino),
    Blackjack("Blackjack", Icons.Filled.Circle),
    Slots("Slots", Icons.Filled.Star)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CasinoHomeScreen(
    uiState: CasinoUiState,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit,
    onPlayRoulette: (Int, RouletteColor) -> Unit,
    onPlayBlackjack: (Int) -> Unit,
    onPlaySlots: (Int) -> Unit
) {
    val selectedTab = rememberSaveable { mutableStateOf(HomeTab.Dashboard) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hola, ${uiState.playerName}") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar {
                HomeTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab.value == tab,
                        onClick = { selectedTab.value = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab.value) {
                HomeTab.Dashboard -> DashboardScreen(uiState)
                HomeTab.Roulette -> RouletteScreen(balance = uiState.balance, onPlay = onPlayRoulette)
                HomeTab.Blackjack -> BlackjackScreen(balance = uiState.balance, onPlay = onPlayBlackjack)
                HomeTab.Slots -> SlotsScreen(balance = uiState.balance, onPlay = onPlaySlots)
            }
        }
    }
}

@Composable
fun DashboardScreen(uiState: CasinoUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Saldo disponible",
            style = MaterialTheme.typography.titleLarge
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "$${uiState.balance}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Historial reciente:",
                    style = MaterialTheme.typography.titleMedium
                )
                if (uiState.history.isEmpty()) {
                    Text("Aún no hay jugadas registradas.")
                } else {
                    uiState.history.forEach { item ->
                        Text(text = "- $item")
                    }
                }
            }
        }
    }
}

@Composable
fun RouletteScreen(balance: Int, onPlay: (Int, RouletteColor) -> Unit) {
    val bet = rememberSaveable { mutableStateOf("50") }
    val selectedColor = rememberSaveable { mutableStateOf(RouletteColor.ROJO) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Saldo: $balance")
        OutlinedTextField(
            value = bet.value,
            onValueChange = { bet.value = it.filter { ch -> ch.isDigit() } },
            label = { Text("Apuesta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RouletteColor.values().forEach { color ->
                val isSelected = selectedColor.value == color
                OutlinedButton(
                    onClick = { selectedColor.value = color },
                    modifier = Modifier.weight(1f)
                ) {
                    val suffix = if (isSelected) " (seleccionado)" else ""
                    Text(color.label + suffix)
                }
            }
        }
        Button(
            onClick = {
                val amount = bet.value.toIntOrNull() ?: 0
                onPlay(amount, selectedColor.value)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Jugar ruleta")
        }
    }
}

@Composable
fun BlackjackScreen(balance: Int, onPlay: (Int) -> Unit) {
    val bet = rememberSaveable { mutableStateOf("100") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Saldo: $balance")
        OutlinedTextField(
            value = bet.value,
            onValueChange = { bet.value = it.filter { ch -> ch.isDigit() } },
            label = { Text("Apuesta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(
            onClick = {
                val amount = bet.value.toIntOrNull() ?: 0
                onPlay(amount)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pedir cartas")
        }
        Text(
            text = "Reglas rápidas: el objetivo es acercarse a 21. El crupier roba automáticamente.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun SlotsScreen(balance: Int, onPlay: (Int) -> Unit) {
    val bet = rememberSaveable { mutableStateOf("25") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Saldo: $balance")
        OutlinedTextField(
            value = bet.value,
            onValueChange = { bet.value = it.filter { ch -> ch.isDigit() } },
            label = { Text("Apuesta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(
            onClick = {
                val amount = bet.value.toIntOrNull() ?: 0
                onPlay(amount)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Girar")
        }
        Text(
            text = "Premios: 3 iguales x4, 2 iguales x2, de lo contrario pierdes tu apuesta.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
