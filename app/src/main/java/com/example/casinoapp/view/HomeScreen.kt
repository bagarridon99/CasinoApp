@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.casinoapp.view

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.casinoapp.R
import com.example.casinoapp.model.CasinoUiState
import com.example.casinoapp.viewmodel.CasinoViewModel
import com.example.casinoapp.model.UserProfile
import com.example.casinoapp.ui.CityBonusCard // widget de bono por ciudad (ubicación)
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import com.example.casinoapp.notification.NotifyHelper

/* ---------------------------------- NAV ---------------------------------- */
// Tabs de la parte inferior (Bottom Navigation)
private enum class HomeTab(val label: String,  val icon: ImageVector) {
    Dashboard("Inicio", Icons.Filled.Home),
    Roulette("Ruleta", Icons.Filled.Casino),
    Blackjack("Blackjack", Icons.Filled.Circle),
    Slots("Slots", Icons.Filled.Star)
}

/* --------------------------------- SCREEN -------------------------------- */
/**
 * Pantalla Home con:
 * - TopBar acciones (historial / logout)
 * - NavBar inferior con 4 secciones
 * - Dashboard: saldo, promos, bono diario, bono por ciudad, estadísticas
 * - Ruleta / Blackjack / Slots como secciones navegables
 */
@Composable
fun HomeScreen(
    viewModel: CasinoViewModel,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit = {}
) {
    // Observa uiState del ViewModel (sin recomponer toda la pantalla en exceso)
    val uiState by remember { derivedStateOf { viewModel.uiState } }
    val selectedTab = rememberSaveable { mutableStateOf(HomeTab.Dashboard) }

    // Diálogos modales de la Home
    var showLimitsDialog by remember { mutableStateOf(false) }
    var showHowToPlayDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showRewardsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CasinoApp") },
                actions = {
                    IconButton(onClick = { showHistoryDialog = true }) {
                        Icon(Icons.Filled.History, contentDescription = "Historial")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        snackbarHost = { AppSnackbar(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                HomeTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab.value == tab,
                        onClick = { selectedTab.value = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize()) {
            // Fondo con efecto Ken Burns + overlay
            CasinoBackgroundHome()

            val contentModifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)

            // Contenido según tab seleccionada
            when (selectedTab.value) {
                HomeTab.Dashboard -> DashboardSection(
                    profile = uiState.profile,
                    uiState = uiState,
                    onDeposit = { viewModel.deposit(it) },
                    onWithdraw = { viewModel.withdraw(it) },
                    onQuickDeposit = { viewModel.deposit(100) },
                    onNavigateTo = { tab -> selectedTab.value = tab },
                    onShowLimits = { showLimitsDialog = true },
                    onShowLearnMore = { showHowToPlayDialog = true },
                    onShowProfile = { showProfileDialog = true },
                    onShowNotifications = { showNotificationsDialog = true },
                    onShowRewards = { showRewardsDialog = true },
                    modifier = contentModifier
                )
                HomeTab.Roulette -> RouletteScreen(
                    uiState = uiState,
                    onPlay = { betAmount, bet -> viewModel.playRoulette(betAmount, bet) },
                    modifier = contentModifier
                )
                HomeTab.Blackjack -> BlackjackScreen(
                    uiState = uiState.blackjackState,
                    balance = uiState.balance,
                    onStartGame = { viewModel.startBlackjack(it) },
                    onHit = { viewModel.blackjackHit() },
                    onStand = { viewModel.blackjackStand() },
                    modifier = contentModifier
                )
                HomeTab.Slots -> SlotsScreen(
                    uiState = uiState,
                    onPlay = { viewModel.playSlots(it) },
                    modifier = contentModifier
                )
            }

            // Diálogos informativos (responsable, cómo jugar, perfil, etc.)
            if (showLimitsDialog) {
                AlertDialog(
                    onDismissRequest = { showLimitsDialog = false },
                    title = { Text("Juego Responsable") },
                    text = { Text("Establece límites de tiempo y dinero. Jugar es entretenimiento...") },
                    confirmButton = { TextButton(onClick = { showLimitsDialog = false }) { Text("Entendido") } }
                )
            }
            if (showHowToPlayDialog) {
                AlertDialog(
                    onDismissRequest = { showHowToPlayDialog = false },
                    title = { Text("Cómo Jugar") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("BlackJack: Intenta sumar 21 sin pasarte.")
                            Text("Máquina Tragamonedas: Gira los rodillos y alinea los símbolos.")
                            Text("Apuestas a Colores: Elige un color y espera el resultado.")
                        }
                    },
                    confirmButton = { TextButton(onClick = { showHowToPlayDialog = false }) { Text("Cerrar") } }
                )
            }
            if (showHistoryDialog) {
                AlertDialog(
                    onDismissRequest = { showHistoryDialog = false },
                    title = { Text("Historial") },
                    text = { Text("Aquí se mostraría el historial global de transacciones y juegos.") },
                    confirmButton = { TextButton(onClick = { showHistoryDialog = false }) { Text("Cerrar") } }
                )
            }
            if (showProfileDialog) {
                AlertDialog(
                    onDismissRequest = { showProfileDialog = false },
                    title = { Text("Perfil de Usuario") },
                    text = { Text("Aquí se mostraría la pantalla de perfil. Saldo actual: ${formatCLP(uiState.balance)}") },
                    confirmButton = { TextButton(onClick = { showProfileDialog = false }) { Text("Cerrar") } }
                )
            }
            if (showNotificationsDialog) {
                AlertDialog(
                    onDismissRequest = { showNotificationsDialog = false },
                    title = { Text("Notificaciones") },
                    text = { Text("Aquí se mostraría la lista de notificaciones") },
                    confirmButton = { TextButton(onClick = { showNotificationsDialog = false }) { Text("Cerrar") } }
                )
            }
            if (showRewardsDialog) {
                AlertDialog(
                    onDismissRequest = { showRewardsDialog = false },
                    title = { Text("Recompensas") },
                    text = { Text("Aquí se mostraría la pantalla de Recompensas y sus niveles. Nivel actual:") },
                    confirmButton = { TextButton(onClick = { showRewardsDialog = false }) { Text("Cerrar") } }
                )
            }
        }
    }
}

/* ------------------------------ DASHBOARD ------------------------------- */
/**
 * Sección principal con:
 * - Perfil + ciudad detectada
 * - Saldo y acciones (depositar/retirar)
 * - Stats rápidas, promos, bono diario, bono por ciudad
 * - Acceso a juegos populares
 */
@Composable
private fun DashboardSection(
    uiState: CasinoUiState,
    profile: UserProfile,
    onDeposit: (Int) -> Unit,
    onWithdraw: (Int) -> Unit,
    onQuickDeposit: () -> Unit,
    onNavigateTo: (HomeTab) -> Unit,
    onShowLimits: () -> Unit,
    onShowLearnMore: () -> Unit,
    onShowProfile: () -> Unit,
    onShowNotifications: () -> Unit,
    onShowRewards: () -> Unit,
    modifier: Modifier = Modifier
) {
    var amount by rememberSaveable { mutableStateOf("100") }
    val amountInt = amount.toIntOrNull() ?: 0
    val canTransact = amountInt > 0

    val context = LocalContext.current
    var lastClaimTs by rememberSaveable { mutableStateOf(DailyBonusStore.getLastClaimTs(context)) }

    // Entra a Home: crea canales y envía notificaciones de demo
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        NotifyHelper.ensureChannels(context)       // crea canales si faltan
        now = System.currentTimeMillis()           // reloj para el bono
        // Ejemplos de notificaciones (demostración de recurso nativo)
        NotifyHelper.sendBonoDiarioNow(context)
        NotifyHelper.sendCityBonusNow(context, "Maipú")
        NotifyHelper.sendRachaNow(context, 3)
    }

    // Tiempo restante para bono diario
    val remainingMillis by remember(lastClaimTs, now) {
        mutableStateOf((BONUS_INTERVAL_MS - (now - lastClaimTs)).coerceAtLeast(0))
    }

    // Reclamar bono: deposita rápido y persiste timestamp
    val claimBonus: () -> Unit = {
        onQuickDeposit()
        val ts = System.currentTimeMillis()
        DailyBonusStore.setLastClaimTs(context, ts)
        lastClaimTs = ts
    }

    // Guardamos ciudad detectada desde CityBonusCard
    var city by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { HomeHeader() }

        // Barra de perfil + badge de notificaciones + ciudad
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                ProfileBar(
                    profile = profile,
                    unread = 2,
                    onProfileClick = onShowProfile,
                    onNotificationsClick = onShowNotifications,
                    city = city
                )
            }
        }

        // Saldo + ingresar monto + depositar/retirar
        item {
            GlassCard {
                BalanceCardContent(
                    balance = uiState.balance,
                    amount = amount,
                    onAmountChange = { input -> amount = input.filter(Char::isDigit) },
                    canTransact = canTransact,
                    onDeposit = { onDeposit(amountInt) },
                    onWithdraw = { onWithdraw(amountInt) }
                )
            }
        }

        // Stats rápidas basadas en history
        item {
            GlassCard {
                QuickStatsRow(history = uiState.history)
            }
        }

        // Banner de promo
        item {
            GlassCard {
                PromoBannerCard(
                    title = "Bono del día",
                    subtitle = "Recibe +20% en tu próximo depósito",
                    cta = "Depositar",
                    onCta = { onDeposit(maxOf(amountInt, 100)) }
                )
            }
        }

        // Bono por ciudad (recurso nativo: ubicación + geocoder)
        item {
            GlassCard {
                CityBonusCard(
                    currentAmount = amountInt,
                    onApplyBonus = { bonus -> onDeposit(bonus) },
                    onCityResolved = { resolved -> city = resolved }
                )
            }
        }

        // Recompensas (nivel/xp)
        item {
            GlassCard {
                RewardsCardContent(
                    profile = profile,
                    onShowRewards = onShowRewards
                )
            }
        }

        // Bono diario con barra de progreso
        item {
            GlassCard {
                DailyBonusCardContent(
                    remainingMillis = remainingMillis,
                    onClaim = claimBonus
                )
            }
        }

        // Juego responsable
        item {
            GlassCard {
                ResponsiblePlayCard(
                    onSetLimits = onShowLimits,
                    onLearnMore = onShowLearnMore
                )
            }
        }

        // Accesos rápidos a juegos
        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Juegos Populares", style = MaterialTheme.typography.titleLarge)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GameCard(
                            "Ruleta", HomeTab.Roulette.icon,
                            onClick = { onNavigateTo(HomeTab.Roulette) },
                            modifier = Modifier.weight(1f)
                        )
                        GameCard(
                            "Blackjack", HomeTab.Blackjack.icon,
                            onClick = { onNavigateTo(HomeTab.Blackjack) },
                            modifier = Modifier.weight(1f)
                        )
                        GameCard(
                            "Slots", HomeTab.Slots.icon,
                            onClick = { onNavigateTo(HomeTab.Slots) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Historial simple
        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Historial reciente", style = MaterialTheme.typography.titleLarge)
                    if (uiState.history.isEmpty()) {
                        EmptyHistory()
                    } else {
                        uiState.history.asReversed().forEach { entry ->
                            HistoryRow(entry)
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

/* ------------------ BACKGROUND + HEADER (coherente login) ------------------ */

@Composable
private fun CasinoBackgroundHome() {
    // Efecto “Ken Burns” suave en imagen de fondo
    val t = rememberInfiniteTransition(label = "kenburnsHome")
    val scale by t.animateFloat(
        initialValue = 1.10f, targetValue = 1.22f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val offsetX by t.animateFloat(
        initialValue = -24f, targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offsetX"
    )
    val offsetY by t.animateFloat(
        initialValue = 8f, targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offsetY"
    )

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ruleta),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = scale; scaleY = scale
                    translationX = offsetX; translationY = offsetY
                }
        )
        // Overlay oscuro para contraste con el contenido
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.62f),
                            Color.Black.copy(alpha = 0.44f),
                            Color.Black.copy(alpha = 0.62f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun HomeHeader() {
    // Logo centrado con “twinkles” (estrellitas) decorativas
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.height(160.dp)) {
            Twinkles(modifier = Modifier.size(220.dp), count = 8)
            Image(
                painter = painterResource(id = R.drawable.logo_casino),
                contentDescription = "Logo CasinoApp",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(180.dp)
            )
        }
    }
}

/* --- GlassCard y bloques de contenido: helpers de UI para consistencia --- */

@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 10.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

