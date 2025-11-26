@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.casinoapp.view

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.R
import com.example.casinoapp.model.CasinoUiState
import com.example.casinoapp.viewmodel.CasinoViewModel
import com.example.casinoapp.model.UserProfile
import com.example.casinoapp.ui.CityBonusCard
import com.example.casinoapp.ui.common.formatCLP
import kotlinx.coroutines.delay

// --- COLORES LOBBY ---
private val LobbyGoldStart = Color(0xFFFFD700)
private val LobbyGoldEnd = Color(0xFFC5A000)
private val LobbyCardGradient = Brush.linearGradient(listOf(Color(0xFF222222), Color(0xFF111111)))

enum class HomeTab { Dashboard, Roulette, Blackjack, Slots }

@Composable
fun HomeScreen(
    viewModel: CasinoViewModel,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit = {}
) {
    val uiState by remember { derivedStateOf { viewModel.uiState } }
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.Dashboard) }

    // Estados de los diálogos
    var showDepositDialog by remember { mutableStateOf(false) }
    var showBonusesDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    // --- AQUÍ ESTÁ LA MAGIA DE LA TRANSICIÓN ---
    AnimatedContent(
        targetState = selectedTab,
        label = "GameTransition",
        transitionSpec = {
            if (targetState == HomeTab.Dashboard) {
                // Si volvemos al Dashboard -> Deslizar hacia la derecha (efecto Back)
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500)) togetherWith
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(500))
            } else {
                // Si vamos a un juego -> Deslizar hacia la izquierda (efecto Next)
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500)) togetherWith
                        slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(500))
            }
        }
    ) { targetTab ->
        when (targetTab) {
            HomeTab.Dashboard -> DashboardLobby(
                uiState = uiState,
                onNavigate = { selectedTab = it },
                onOpenDeposit = { showDepositDialog = true },
                onOpenBonuses = { showBonusesDialog = true },
                onOpenHistory = { showHistoryDialog = true },
                onLogout = onLogout
            )
            HomeTab.Roulette -> RouletteScreen(
                uiState = uiState,
                onPlay = { amt, bet -> viewModel.playRoulette(amt, bet) },
                modifier = Modifier.fillMaxSize()
            )
            HomeTab.Blackjack -> BlackjackScreen(
                uiState = uiState.blackjackState,
                balance = uiState.balance,
                onStartGame = { viewModel.startBlackjack(it) },
                onHit = { viewModel.blackjackHit() },
                onStand = { viewModel.blackjackStand() },
                modifier = Modifier.fillMaxSize()
            )
            HomeTab.Slots -> SlotsScreen(
                uiState = uiState,
                onPlay = { viewModel.playSlots(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // --- DIÁLOGOS ---
    if (showDepositDialog) {
        DepositSheet(
            currentBalance = uiState.balance,
            onDismiss = { showDepositDialog = false },
            onDeposit = { viewModel.deposit(it); showDepositDialog = false },
            onWithdraw = { viewModel.withdraw(it); showDepositDialog = false }
        )
    }

    if (showBonusesDialog) {
        BonusesSheet(
            profile = uiState.profile,
            onDismiss = { showBonusesDialog = false },
            onClaimDaily = {
                viewModel.claimDailyBonus()
                showBonusesDialog = false
            },
            onDepositBonus = { viewModel.deposit(it) }
        )
    }

    if (showHistoryDialog) {
        HistorySheet(
            history = uiState.history,
            onDismiss = { showHistoryDialog = false }
        )
    }

    // Botón flotante para volver al Lobby (solo si estamos en un juego)
    if (selectedTab != HomeTab.Dashboard) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopStart) {
            SmallFloatingActionButton(
                onClick = { selectedTab = HomeTab.Dashboard },
                containerColor = Color.Black.copy(0.7f),
                contentColor = LobbyGoldStart
            ) {
                Icon(Icons.Default.ArrowBack, "Volver")
            }
        }
    }
}

@Composable
fun DashboardLobby(
    uiState: CasinoUiState,
    onNavigate: (HomeTab) -> Unit,
    onOpenDeposit: () -> Unit,
    onOpenBonuses: () -> Unit,
    onOpenHistory: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_casino),
                    contentDescription = "Logo CasinoApp",
                    modifier = Modifier.height(56.dp).widthIn(max = 180.dp),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.CenterStart
                )
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.Logout, null, tint = Color.Gray)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { WalletCard(uiState.profile, uiState.balance, onOpenDeposit) }
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    QuickAction(Icons.Default.Add, "Depositar", onOpenDeposit)
                    QuickAction(Icons.Default.Star, "Bonos", onOpenBonuses)
                    QuickAction(Icons.Outlined.History, "Historial", onOpenHistory)
                    QuickAction(Icons.Outlined.Info, "Ayuda") { }
                }
            }
            item {
                Text(
                    "SALA DE JUEGOS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                )
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    GameBanner("RULETA EUROPEA", "Clásica y Elegante", R.drawable.roulette_background) { onNavigate(HomeTab.Roulette) }
                    GameBanner("BLACKJACK VIP", "Vence al crupier", R.drawable.blackjack_background) { onNavigate(HomeTab.Blackjack) }
                    GameBanner("SUPER SLOTS", "Jackpots millonarios", R.drawable.slots_background) { onNavigate(HomeTab.Slots) }
                }
            }
        }
    }
}

// --- COMPONENTES (Sin cambios importantes, solo integrados) ---

@Composable
fun HistorySheet(history: List<String>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF222222),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.History, null, tint = LobbyGoldStart)
                Spacer(Modifier.width(12.dp))
                Text("Historial de Movimientos", color = Color.White, fontSize = 18.sp)
            }
        },
        text = {
            if (history.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No hay movimientos recientes", color = Color.Gray)
                }
            } else {
                Box(Modifier.heightIn(max = 300.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(history.size) { idx ->
                            HistoryItemRow(history[idx])
                            Divider(color = Color.White.copy(0.1f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar", color = Color.White) } }
    )
}

@Composable
fun HistoryItemRow(text: String) {
    val isPositive = text.contains("Ganó", ignoreCase = true) || text.contains("Depósito", ignoreCase = true) || text.contains("Bono", ignoreCase = true) || text.contains("+")
    val icon = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFEF5350)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = color.copy(alpha = 0.1f), shape = CircleShape, modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(16.dp)) }
        }
        Spacer(Modifier.width(12.dp))
        Text(text = text, color = Color.LightGray, fontSize = 13.sp, lineHeight = 16.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
fun WalletCard(profile: UserProfile, balance: Int, onDeposit: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(180.dp).shadow(12.dp, RoundedCornerShape(24.dp)).background(LobbyCardGradient, RoundedCornerShape(24.dp)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawCircle(Color(0xFFFFD700).copy(0.05f), radius = 300f, center = center.copy(x = size.width)) }
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("SALDO DISPONIBLE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(formatCLP(balance), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Surface(color = Color(0xFF333333), shape = RoundedCornerShape(50)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, null, tint = LobbyGoldStart, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("LVL ${profile.nivel}", color = LobbyGoldStart, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(profile.nombre.uppercase(), color = Color.White.copy(0.7f), fontSize = 14.sp, letterSpacing = 1.sp)
                Button(onClick = onDeposit, colors = ButtonDefaults.buttonColors(containerColor = LobbyGoldStart), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp), modifier = Modifier.height(36.dp)) {
                    Text("AGREGAR", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(56.dp).background(Color(0xFF222222), CircleShape).border(1.dp, Color.White.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White)
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun GameBanner(title: String, subtitle: String, imageRes: Int, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().height(140.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp)) {
        Box {
            Image(painter = painterResource(imageRes), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(0.9f), Color.Transparent))))
            Column(Modifier.align(Alignment.CenterStart).padding(20.dp), verticalArrangement = Arrangement.Center) {
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(subtitle, color = LobbyGoldStart, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Surface(color = Color.White.copy(0.2f), shape = RoundedCornerShape(4.dp)) {
                    Text("JUGAR AHORA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun DepositSheet(currentBalance: Int, onDismiss: () -> Unit, onDeposit: (Int) -> Unit, onWithdraw: (Int) -> Unit) {
    var amount by remember { mutableStateOf("1000") }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFF222222),
        title = { Text("Gestionar Fondos", color = Color.White) },
        text = {
            Column {
                Text("Saldo actual: ${formatCLP(currentBalance)}", color = Color.Gray)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it.filter(Char::isDigit) }, label = { Text("Monto") }, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LobbyGoldStart, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1000, 5000, 10000).forEach { SuggestionChip(onClick = { amount = it.toString() }, label = { Text("$it") }) }
                }
            }
        },
        confirmButton = { Button(onClick = { onDeposit(amount.toIntOrNull() ?: 0) }, colors = ButtonDefaults.buttonColors(containerColor = LobbyGoldStart)) { Text("Depositar", color = Color.Black) } },
        dismissButton = { TextButton(onClick = { onWithdraw(amount.toIntOrNull() ?: 0) }) { Text("Retirar", color = Color.Red) } }
    )
}

@Composable
fun BonusesSheet(profile: UserProfile, onDismiss: () -> Unit, onClaimDaily: () -> Unit, onDepositBonus: (Int) -> Unit) {
    val context = LocalContext.current
    var remainingMs by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE)
        while(true) {
            val last = prefs.getLong("last_bonus_ts", 0L)
            val now = System.currentTimeMillis()
            val diff = now - last
            val waitTime = 86400000L
            if (diff < waitTime) remainingMs = waitTime - diff else remainingMs = 0L
            delay(1000L)
        }
    }
    val isAvailable = remainingMs <= 0L
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = Color(0xFF222222),
        title = { Text("Centro de Recompensas", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF333333))) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null, tint = LobbyGoldStart)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Bono Diario", color = Color.White, fontWeight = FontWeight.Bold)
                            if (isAvailable) Text("¡Disponible ahora!", color = LobbyGoldStart, fontSize = 12.sp)
                            else {
                                val h = remainingMs / 3600000L
                                val m = (remainingMs % 3600000L) / 60000L
                                Text("Espera ${h}h ${m}m", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        Button(onClick = onClaimDaily, enabled = isAvailable, colors = ButtonDefaults.buttonColors(containerColor = LobbyGoldStart, disabledContainerColor = Color.DarkGray)) {
                            Text("Reclamar", color = if(isAvailable) Color.Black else Color.Gray)
                        }
                    }
                }
                CityBonusCard(currentAmount = 0, onApplyBonus = { onDepositBonus(it) }, onCityResolved = { }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar", color = Color.White) } }
    )
}