@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.casinoapp.view

import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.R
import com.example.casinoapp.model.BlackjackGameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.ui.res.painterResource

/* =======================================================================
 *  BLACKJACK
 *  Pantalla principal del juego de Blackjack.
 *  - Muestra saldo y permite apostar.
 *  - Controla manos de jugador y crupier.
 *  - Anima estados (confetti, banners) y reproduce SFX.
 * ======================================================================= */

@Composable
fun BlackjackScreen(
    uiState: BlackjackGameState,     // Estado del juego (manos, turno, mensajes)
    balance: Int,                    // Saldo actual del usuario para validar apuestas
    onStartGame: (Int) -> Unit,      // Inicio de mano nueva con apuesta
    onHit: () -> Unit,               // Pedir carta
    onStand: () -> Unit,             // Plantarse
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    // --- Inicializa players de sonido (si no existen recursos no falla) ---
    val sfx = remember {
        SoundFx(
            shuffle = tryCreatePlayer(context, "shuffle"),
            flip = tryCreatePlayer(context, "flip"),
            win = tryCreatePlayer(context, "win"),
            lose = tryCreatePlayer(context, "lose")
        )
    }
    // Libera SFX al salir de la pantalla
    DisposableEffect(Unit) { onDispose { sfx.release() } }

    // Apuesta editable con estado guardable (sobre recomposiciones)
    var bet by rememberSaveable { mutableStateOf("100") }
    val betInt = bet.toIntOrNull() ?: 0
    val canStart = betInt in 1..balance
    val isGameInProgress = uiState.isPlayerTurn

    // Estado de resultado visual (banner + confetti)
    var outcomeText by remember { mutableStateOf<String?>(null) }
    var outcomePositive by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    // Reacciona a cambios: mensajes del juego, fin de mano, etc.
    LaunchedEffect(isGameInProgress, uiState.gameMessage) {
        if (!isGameInProgress && uiState.playerHand.isNotEmpty()) {
            sfx.flip.startSafely()
        }
        uiState.gameMessage?.let { msg ->
            outcomeText = msg
            when {
                // Casos de victoria / blackjack
                msg.contains("Ganaste", true) || msg.contains("Blackjack", true) -> {
                    outcomePositive = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    sfx.win.startSafely()
                    showConfetti = true
                    launch {
                        delay(3800); showConfetti = false
                    }
                }
                // Derrota
                msg.contains("Perdiste", true) || msg.contains("pierdes", true) -> {
                    outcomePositive = false
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    sfx.lose.startSafely()
                }
                // Empate
                msg.contains("Empate", true) || msg.contains("Push", true) -> {
                    outcomePositive = false
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
            launch {
                delay(4200); outcomeText = null
            }
        }
    }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            /* ------------------------- HEADER ------------------------- */
            item { GameHeader(imageRes = R.drawable.blackjack_background, balance = balance) }

            /* ------------------ PANEL DE APUESTA ---------------------- */
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.1f)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Solo disponible cuando no está en curso la mano
                        AnimatedVisibility(!isGameInProgress) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Coloca tu apuesta", style = MaterialTheme.typography.titleMedium)
                                OutlinedTextField(
                                    value = bet,
                                    onValueChange = { bet = it.filter(Char::isDigit) }, // solo dígitos
                                    label = { Text("Monto") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    supportingText = {
                                        if (!canStart) Text(
                                            "Debe ser mayor a 0 y no superar tu saldo.",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                                // Atajos de apuesta rápida
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(50, 100, 200, 500).forEach { preset ->
                                        AssistChip(onClick = { bet = preset.toString() }, label = { Text("\$${preset}") })
                                    }
                                    AssistChip(onClick = { bet = balance.toString() }, label = { Text("MAX") })
                                }
                                // Iniciar mano
                                Button(
                                    onClick = { sfx.shuffle.startSafely(); onStartGame(betInt) },
                                    enabled = canStart,
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) { Text("Repartir cartas") }
                            }
                        }
                    }
                }
            }

            /* ------------------------- MESA --------------------------- */
            if (uiState.playerHand.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.1f))
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // --- Crupier: muestra suma parcial si la mano sigue en curso ---
                            val dealerShownTotal =
                                if (isGameInProgress) "${cardValueToString(uiState.dealerHand.first())} + ?"
                                else handTotal(uiState.dealerHand).toString()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Mano del crupier", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                TotalBadge(
                                    total = if (isGameInProgress) null else handTotal(uiState.dealerHand),
                                    isBlackjack = if (isGameInProgress) false else isBlackjack(uiState.dealerHand),
                                    isBust = if (isGameInProgress) false else isBust(uiState.dealerHand),
                                    hintText = dealerShownTotal
                                )
                            }
                            // Segunda carta oculta mientras el jugador juega
                            CardsRow(dealer = true, hiddenSecond = isGameInProgress, cards = uiState.dealerHand)

                            // --- Jugador ---
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Tu mano", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                TotalBadge(
                                    total = handTotal(uiState.playerHand),
                                    isBlackjack = isBlackjack(uiState.playerHand),
                                    isBust = isBust(uiState.playerHand),
                                )
                            }
                            CardsRow(cards = uiState.playerHand)

                            // --- Mensaje y acciones ---
                            Spacer(Modifier.height(8.dp))
                            uiState.gameMessage?.let {
                                Text(it, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }

                            // Acciones mientras es turno del jugador
                            AnimatedVisibility(isGameInProgress) {
                                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Button(
                                        onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); sfx.flip.startSafely(); onHit() },
                                        modifier = Modifier.weight(1f).height(50.dp)
                                    ) { Text("Pedir") }
                                    Button(
                                        onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onStand() },
                                        modifier = Modifier.weight(1f).height(50.dp)
                                    ) { Text("Plantarse") }
                                }
                            }

                            // Botón para nueva mano al terminar
                            AnimatedVisibility(!isGameInProgress) {
                                Button(
                                    onClick = { sfx.shuffle.startSafely(); onStartGame((bet.toIntOrNull() ?: 0).coerceAtLeast(1)) },
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) { Text("Nueva mano") }
                            }
                        }
                    }
                }
            }

            // Banda con outcome (aparece/oculta animado)
            item {
                AnimatedVisibility(visible = outcomeText != null) {
                    OutcomeBanner(text = outcomeText ?: "", positive = outcomePositive)
                }
            }
        }

        // Confetti superpuesto cuando hay victoria
        ConfettiOverlay(visible = showConfetti)
    }
}

/* ========================= SUB-COMPONENTES UI ========================= */

@Composable
private fun GameHeader(imageRes: Int, balance: Int) {
    Card(
        Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.1f))
    ) {
        Box {
            Image(painter = painterResource(imageRes), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))
            Text("Saldo: ${formatCLP(balance)}", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp))
        }
    }
}

@Composable
private fun CardsRow(cards: List<Int>, dealer: Boolean = false, hiddenSecond: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.height(74.dp)) {
        if (cards.isEmpty()) return
        CardView(cardValueToString(cards.first()))
        if (hiddenSecond && cards.size >= 2) HiddenCardView() else cards.drop(1).forEach { CardView(cardValueToString(it)) }
    }
}

@Composable private fun HiddenCardView() {
    Card(Modifier.size(52.dp, 74.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {}
}

@Composable private fun CardView(value: String) {
    Card(Modifier.size(52.dp, 74.dp), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(0.3f))) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TotalBadge(total: Int?, isBlackjack: Boolean, isBust: Boolean, hintText: String? = null) {
    val (bg, fg, label) = when {
        isBlackjack -> Triple(Color(0xFF1B5E20), Color.White, "Blackjack")
        isBust -> Triple(Color(0xFFB71C1C), Color.White, "Bust")
        else -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, null)
    }
    Surface(color = bg, contentColor = fg, shape = RoundedCornerShape(999.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (label != null) Text(label, fontWeight = FontWeight.SemiBold)
            if (total != null) {
                if (label != null) Spacer(Modifier.width(8.dp))
                Text("Total: $total")
            } else if (hintText != null) {
                Text(hintText)
            }
        }
    }
}

@Composable
private fun OutcomeBanner(text: String, positive: Boolean) {
    val bg = if (positive) Color(0xFF1B5E20) else Color(0xFFB71C1C)
    Surface(color = bg, contentColor = Color.White, shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        Text(text = text, modifier = Modifier.padding(14.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
    }
}

/* ----------------------------- CONFETTI -------------------------------- */
// Overlay con piezas animadas cayendo; usa InfiniteTransition para loops.

@Composable
private fun ConfettiOverlay(visible: Boolean) {
    if (!visible) return
    val t = rememberInfiniteTransition(label = "confetti")
    val pieces = remember { List(30) { ConfettiPiece.random() } }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        pieces.forEachIndexed { i, p ->
            val fall by t.animateFloat(
                initialValue = -100f, targetValue = 1400f,
                animationSpec = infiniteRepeatable(tween(p.duration, i * 80, FastOutSlowInEasing), RepeatMode.Restart),
                label = "fall$i"
            )
            val rot by t.animateFloat(
                initialValue = 0f, targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(p.duration, easing = FastOutSlowInEasing), RepeatMode.Restart),
                label = "rot$i"
            )
            Box(
                Modifier
                    .offset(x = p.startX.dp, y = fall.dp)
                    .size(p.size.dp)
                    .rotate(rot)
                    .alpha(0.9f)
                    .background(p.color, shape = RoundedCornerShape(3.dp))
            )
        }
    }
}

// Pieza de confetti con color/velocidad aleatoria
private data class ConfettiPiece(val startX: Int, val size: Int, val color: Color, val duration: Int) {
    companion object {
        fun random(): ConfettiPiece {
            val colors = listOf(
                Color(0xFFF44336), Color(0xFF9C27B0), Color(0xFF2196F3),
                Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFFFF9800)
            )
            return ConfettiPiece(
                startX = Random.nextInt(-40, 360),
                size = Random.nextInt(6, 12),
                color = colors.random(),
                duration = Random.nextInt(1800, 2800)
            )
        }
    }
}

/* ------------------------------ HELPERS -------------------------------- */
// Utilidades de Blackjack: cálculo de totales, formateo CLP y SFX.

private fun isBlackjack(cards: List<Int>) = cards.size == 2 && handTotal(cards) == 21
private fun isBust(cards: List<Int>) = handTotal(cards) > 21

private fun cardValueToString(value: Int) = when (value) { 11 -> "A"; 10 -> "K"; else -> value.toString() }

private fun handTotal(cards: List<Int>): Int {
    var total = cards.sum()
    var aces = cards.count { it == 11 }
    while (total > 21 && aces > 0) { total -= 10; aces-- } // baja As de 11 a 1
    return total
}

private fun formatCLP(value: Int): String {
    val nf = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "CL"))
    return nf.format(value)
}

/* ------------------------------ SFX UTILS ------------------------------- */

private data class SoundFx(
    val shuffle: MediaPlayer?, val flip: MediaPlayer?, val win: MediaPlayer?, val lose: MediaPlayer?
) {
    fun release() { listOf(shuffle, flip, win, lose).forEach { it?.release() } }
}

private fun tryCreatePlayer(context: android.content.Context, rawName: String): MediaPlayer? {
    return try {
        val id = context.resources.getIdentifier(rawName, "raw", context.packageName)
        if (id == 0) null else MediaPlayer.create(context, id)
    } catch (_: Throwable) { null }
}

private fun MediaPlayer?.startSafely() {
    try { this?.let { it.seekTo(0); it.start() } } catch (_: Throwable) { }
}
