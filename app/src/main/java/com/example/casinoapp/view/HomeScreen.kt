package com.example.casinoapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.casinoapp.model.CasinoUiState
import com.example.casinoapp.viewmodel.CasinoViewModel

private enum class HomeTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Dashboard("Inicio", Icons.Filled.Home),
    Roulette("Ruleta", Icons.Filled.Casino),
    Blackjack("Blackjack", Icons.Filled.Circle),
    Slots("Slots", Icons.Filled.Star)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CasinoViewModel,
    snackbarHostState: SnackbarHostState,
) {
    val uiState by remember { derivedStateOf { viewModel.uiState } }
    val selectedTab = rememberSaveable { mutableStateOf(HomeTab.Dashboard) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Hola, ${uiState.playerName}") },
                actions = { IconButton(onClick = { viewModel.logout() }) { Icon(Icons.Filled.Logout, "Cerrar sesión") } }
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
                HomeTab.Dashboard -> DashboardSection(
                    uiState = uiState,
                    onDeposit = { viewModel.deposit(it) },
                    onWithdraw = { viewModel.withdraw(it) }
                )
                HomeTab.Roulette -> RouletteScreen(
                    uiState = uiState,
                    onPlay = { betAmount, bet -> viewModel.playRoulette(betAmount, bet) }
                )
                HomeTab.Blackjack -> BlackjackScreen(
                    uiState = uiState.blackjackState,
                    balance = uiState.balance,
                    onStartGame = { viewModel.startBlackjack(it) },
                    onHit = { viewModel.blackjackHit() },
                    onStand = { viewModel.blackjackStand() }
                )
                HomeTab.Slots -> SlotsScreen(
                    uiState = uiState,
                    onPlay = { viewModel.playSlots(it) }
                )
            }
        }
    }
}

@Composable
private fun DashboardSection(
    uiState: CasinoUiState,
    onDeposit: (Int) -> Unit,
    onWithdraw: (Int) -> Unit
) {
    var amount by rememberSaveable { mutableStateOf("100") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Saldo disponible", style = MaterialTheme.typography.titleLarge)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("$${uiState.balance}", style = MaterialTheme.typography.headlineLarge)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input -> amount = input.filter(Char::isDigit) },
                    label = { Text("Monto") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onDeposit(amount.toIntOrNull() ?: 0) }, modifier = Modifier.weight(1f)) {
                        Text("Depositar")
                    }
                    OutlinedButton(onClick = { onWithdraw(amount.toIntOrNull() ?: 0) }, modifier = Modifier.weight(1f)) {
                        Text("Retirar")
                    }
                }
                Divider()
                Text("Historial reciente", style = MaterialTheme.typography.titleMedium)
                if (uiState.history.isEmpty()) {
                    Text("Aún no hay movimientos.")
                } else {
                    uiState.history.forEach { Text("• $it") }
                }
            }
        }
    }
}