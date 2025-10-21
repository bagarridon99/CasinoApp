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
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

/* ---------------------------------- NAV ---------------------------------- */

private enum class HomeTab(val label: String, val icon: ImageVector) {
    Dashboard("Inicio", Icons.Filled.Home),
    Roulette("Ruleta", Icons.Filled.Casino),
    Blackjack("Blackjack", Icons.Filled.Circle),
    Slots("Slots", Icons.Filled.Star)
}

/* ------------------------------- USER MOCK ------------------------------- */

/*private data class UserProfile(
    val nombre: String = "Basti",
    val nivel: Int = 3,
    val xpActual: Int = 45, // 0..100
)*/

/* --------------------------------- SCREEN -------------------------------- */

@Composable
fun HomeScreen(
    viewModel: CasinoViewModel,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit = {}
) {
    val uiState by remember { derivedStateOf { viewModel.uiState } }
    val selectedTab = rememberSaveable { mutableStateOf(HomeTab.Dashboard) }

    // --- (MODIFICADO) ESTADOS PARA TODOS LOS DIÁLOGOS ---
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
                    IconButton(onClick = { showHistoryDialog = true }) { // <-- CAMBIO
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
            CasinoBackgroundHome()

            val contentModifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)

            when (selectedTab.value) {
                HomeTab.Dashboard -> DashboardSection(
                    profile = uiState.profile,
                    uiState = uiState,
                    onDeposit = { viewModel.deposit(it) },
                    onWithdraw = { viewModel.withdraw(it) },
                    onQuickDeposit = { viewModel.deposit(100) },
                    onNavigateTo = { tab -> selectedTab.value = tab },

                    // --- (MODIFICADO) Pasar todas las acciones ---
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
            // Diálogos de Juego Responsable
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

            // Nuevos Diálogos
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

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val remainingMillis by remember(lastClaimTs, now) {
        mutableStateOf((BONUS_INTERVAL_MS - (now - lastClaimTs)).coerceAtLeast(0))
    }

    val claimBonus: () -> Unit = {
        onQuickDeposit()
        val ts = System.currentTimeMillis()
        DailyBonusStore.setLastClaimTs(context, ts)
        lastClaimTs = ts
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { HomeHeader() }

        item {
            ProfileBar(
                profile = profile,
                unread = 2,
                onProfileClick = onShowProfile,
                onNotificationsClick = onShowNotifications
            )
        }

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

        item {
            GlassCard {
                QuickStatsRow(history = uiState.history)
            }
        }

        item {
            PromoBannerCard(
                title = "Bono del día",
                subtitle = "Recibe +20% en tu próximo depósito",
                cta = "Depositar",
                onCta = { onDeposit(maxOf(amountInt, 100)) }
            )
        }

        item {
            GlassCard {
                RewardsCardContent(
                    profile = profile,
                    onShowRewards = onShowRewards
                )
            }
        }

        item {
            GlassCard {
                DailyBonusCardContent(
                    remainingMillis = remainingMillis,
                    onClaim = claimBonus
                )
            }
        }

        item {
            GlassCard {
                ResponsiblePlayCard(
                    onSetLimits = onShowLimits,
                    onLearnMore = onShowLearnMore
                )
            }
        }

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

/* --------------------------------- CARDS --------------------------------- */

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

/* ----------------------------- CONTENT BLOCKS ---------------------------- */

@Composable
private fun ProfileBar(
    profile: UserProfile,
    unread: Int,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ElevatedCard(onClick = onProfileClick, shape = MaterialTheme.shapes.large) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = "Perfil", modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Hola, ${profile.nombre}", style = MaterialTheme.typography.titleMedium)
            Text("¡Que la suerte te acompañe!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        BadgedBox(
            badge = { if (unread > 0) Badge { Text(unread.coerceAtMost(99).toString()) } },
            modifier = Modifier.padding(end = 2.dp)
        ) {
            IconButton(onClick = onNotificationsClick) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
            }
        }
    }
}

/* ===== Estadísticas rápidas ===== */

@Composable
private fun QuickStatsRow(history: List<String>) {
    val stats = remember(history) { computeStats(history) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            title = "Victorias",
            value = stats.wins.toString(),
            icon = Icons.Filled.ThumbUp,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Derrotas",
            value = stats.losses.toString(),
            icon = Icons.Filled.ThumbDown,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Racha",
            value = (if (stats.streak > 0) "+${stats.streak}" else "${stats.streak}"),
            icon = Icons.Filled.TrendingUp,
            modifier = Modifier.weight(1f)
        )
    }
}

private data class Stats(val wins: Int, val losses: Int, val streak: Int)

private fun computeStats(history: List<String>): Stats {
    var wins = 0
    var losses = 0
    var streak = 0
    var current = 0
    history.forEach { row ->
        when {
            row.contains("Ganó", ignoreCase = true) -> { wins++; current = if (current >= 0) current + 1 else 1 }
            row.contains("Perdió", ignoreCase = true) -> { losses++; current = if (current <= 0) current - 1 else -1 }
        }
        streak = if (kotlin.math.abs(current) > kotlin.math.abs(streak)) current else streak
    }
    return Stats(wins, losses, streak)
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.height(84.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, contentDescription = title)
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/* ===== Banner de promoción ===== */

@Composable
private fun PromoBannerCard(
    title: String,
    subtitle: String,
    cta: String,
    onCta: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.80f)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Filled.CardGiftcard, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Button(onClick = onCta, shape = MaterialTheme.shapes.large) { Text(cta) }
        }
    }
}

/* ===== Juego responsable ===== */

@Composable
private fun ResponsiblePlayCard(
    onSetLimits: () -> Unit,
    onLearnMore: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Juego responsable", style = MaterialTheme.typography.titleLarge)
        Text(
            "Establece límites y toma descansos. Jugar es entretenimiento, no una forma de ingresos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AssistChip(onClick = onSetLimits, label = { Text("Establecer límites") }, leadingIcon = { Icon(Icons.Filled.Timer, null) })
            AssistChip(onClick = onLearnMore, label = { Text("Aprender más") }, leadingIcon = { Icon(Icons.Filled.Info, null) })
        }
    }
}

/* ----------------------------- BONO DIARIO ----------------------------- */

@Composable
private fun RewardsCardContent(profile: UserProfile, onShowRewards: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Recompensas", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        AssistChip(onClick = onShowRewards, label = { Text("Ver") })
    }
    Spacer(Modifier.height(10.dp))
    Text("Nivel ${profile.nivel} • ${profile.xpActual}%")
    LinearProgressIndicator(
        progress = profile.xpActual / 100f,
        modifier = Modifier.fillMaxWidth(),
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
    Text(
        "Juega para subir de nivel y desbloquear bonos.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun DailyBonusCardContent(
    remainingMillis: Long,
    onClaim: () -> Unit
) {
    val available = remainingMillis <= 0L
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Bono diario", style = MaterialTheme.typography.titleLarge)
                Text(
                    if (available) "Reclama $${BONUS_AMOUNT} gratis hoy."
                    else "Disponible en ${formatHMS(remainingMillis)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = onClaim, enabled = available, shape = MaterialTheme.shapes.large) {
                Text(if (available) "Reclamar" else "Esperando…")
            }
        }
        if (!available) {
            Spacer(Modifier.height(8.dp))
            DailyBonusProgress(remainingMs = remainingMillis, totalMs = BONUS_INTERVAL_MS)
        }
    }
}

@Composable
private fun DailyBonusProgress(remainingMs: Long, totalMs: Long) {
    val fraction = 1f - (remainingMs.toFloat() / totalMs.coerceAtLeast(1))
    LinearProgressIndicator(
        progress = fraction.coerceIn(0f, 1f),
        modifier = Modifier.fillMaxWidth(),
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

/* ------------------------------ BALANCE ------------------------------ */

@Composable
private fun BalanceCardContent(
    balance: Int,
    amount: String,
    onAmountChange: (String) -> Unit,
    canTransact: Boolean,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit
) {
    Text("Saldo disponible", style = MaterialTheme.typography.titleLarge)
    Row(verticalAlignment = Alignment.CenterVertically) {
        AnimatedCurrency(balance)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Outlined.AccountBalanceWallet, contentDescription = null)
    }
    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        label = { Text("Monto") },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(6.dp))
    QuickAmountChips(
        current = amount,
        balance = balance,
        onPick = { picked -> onAmountChange(picked.toString()) }
    )
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onDeposit, enabled = canTransact, modifier = Modifier.weight(1f)) {
            Text("Depositar")
        }
        FilledTonalButton(onClick = onWithdraw, enabled = canTransact, modifier = Modifier.weight(1f)) {
            Text("Retirar")
        }
    }
}

@Composable
private fun QuickAmountChips(
    current: String,
    balance: Int,
    onPick: (Int) -> Unit
) {
    val presets = listOf(50, 100, 200, 500)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.forEach { v ->
            AssistChip(onClick = { onPick(v) }, label = { Text("\$${v}") })
        }
        AssistChip(onClick = { onPick(balance) }, label = { Text("MAX") })
    }
}

/* ------------------------------ GAME CARD ------------------------------ */

@Composable
private fun GameCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "gcScale"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        try { tryAwaitRelease() } finally { pressed = false }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/* ----------------------------- HISTORIAL ----------------------------- */

@Composable
private fun EmptyHistory() {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.History, null, modifier = Modifier.size(42.dp))
        Spacer(Modifier.height(8.dp))
        Text("Aún no hay movimientos", fontWeight = FontWeight.SemiBold)
        Text("Juega o deposita para ver tu historial.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun HistoryRow(raw: String) {
    val title = raw.substringBefore(":").ifBlank { raw }
    val amountPart = raw.substringAfter(": ", "").substringBefore(" (")
    val datePart = raw.substringAfter("(", "").substringBeforeLast(")")
    val isPositive = title.startsWith("Depósito") || title.contains("Ganó", ignoreCase = true)
    val amountColor = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)

    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        trailingContent = { if (amountPart.isNotEmpty()) Text(amountPart, color = amountColor, fontWeight = FontWeight.Bold) },
        supportingContent = { if (datePart.isNotEmpty()) Text(datePart) }
    )
}

/* ------------------------------- SNACKBAR ------------------------------- */

@Composable
private fun AppSnackbar(host: SnackbarHostState) {
    SnackbarHost(host) { data ->
        val isError = data.visuals.withDismissAction
        val bg = if (isError) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.inverseSurface
        val fg = if (isError) MaterialTheme.colorScheme.onErrorContainer
        else MaterialTheme.colorScheme.inverseOnSurface

        Snackbar(
            containerColor = bg,
            contentColor = fg,
            snackbarData = data,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/* ----------------------------- ANIM UTILITIES ----------------------------- */

@Composable
private fun AnimatedCurrency(amount: Int) {
    var target by remember { mutableIntStateOf(amount) }
    LaunchedEffect(amount) { target = amount }
    val animated by animateIntAsState(targetValue = target, label = "cash")
    Text(formatCLP(animated), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
}

/* ---------------------------------- UTILS --------------------------------- */

private fun formatCLP(value: Int): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(value)

// ---- Bono diario: persistencia y utilidades ----
private const val BONUS_INTERVAL_MS = 24L * 60 * 60 * 1000  // 24 h en ms
private const val BONUS_AMOUNT = 100
private const val PREFS_NAME = "casino_prefs"
private const val KEY_LAST_BONUS_TS = "last_bonus_ts"

private object DailyBonusStore {
    fun getLastClaimTs(context: android.content.Context): Long {
        val sp = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        return sp.getLong(KEY_LAST_BONUS_TS, 0L)
    }
    fun setLastClaimTs(context: android.content.Context, ts: Long) {
        val sp = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        sp.edit().putLong(KEY_LAST_BONUS_TS, ts).apply()
    }
}

private fun formatHMS(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

/* ---- Twinkles para el header (mismas estrellitas del login) ---- */
@Composable
private fun Twinkles(modifier: Modifier = Modifier, count: Int = 8) {
    val t = rememberInfiniteTransition(label = "twk")
    val delays = remember { List(count) { 150 * it } }
    val anims = delays.mapIndexed { i, d ->
        t.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400 + d, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "a$i"
        )
    }
    Box(
        modifier = modifier.drawBehind {
            val w = size.width; val h = size.height
            val points = listOf(
                Offset(w*0.18f, h*0.30f), Offset(w*0.82f, h*0.32f),
                Offset(w*0.12f, h*0.55f), Offset(w*0.88f, h*0.58f),
                Offset(w*0.35f, h*0.18f), Offset(w*0.65f, h*0.16f),
                Offset(w*0.25f, h*0.72f), Offset(w*0.75f, h*0.74f),
                Offset(w*0.50f, h*0.10f), Offset(w*0.50f, h*0.82f)
            ).take(count)

            points.forEachIndexed { i, p ->
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = anims[i].value * 0.85f),
                    radius = 5f,
                    center = p
                )
            }
        }
    )
}