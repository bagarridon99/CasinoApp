@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.casinoapp.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.R
import com.example.casinoapp.model.BlackjackGameState
import com.example.casinoapp.ui.common.GameHeader
import com.example.casinoapp.ui.common.ResultBanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import android.media.MediaPlayer
import androidx.compose.foundation.shape.CircleShape // <--- AGREGA ESTA LÍNEA
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip


// --- Colores VIP Blackjack ---
private val FeltGreen = Color(0xFF0F3D0F) // Verde oscuro paño
private val FeltGreenDark = Color(0xFF051C05)
private val CardWhite = Color(0xFFF0F0F0)
private val GoldAccent = Color(0xFFFFD700)
private val GoldDark = Color(0xFFC5A000)

private val TableGradient = Brush.radialGradient(
    colors = listOf(FeltGreen, FeltGreenDark),
    radius = 1200f
)
private val GoldBorder = Brush.verticalGradient(listOf(GoldDark, GoldAccent, GoldDark))

@Composable
fun BlackjackScreen(
    uiState: BlackjackGameState,
    balance: Int,
    onStartGame: (Int) -> Unit,
    onHit: () -> Unit,
    onStand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    // SFX (Igual que antes)
    val sfx = remember {
        SoundFx(
            shuffle = tryCreatePlayer(context, "shuffle"),
            flip = tryCreatePlayer(context, "flip"),
            win = tryCreatePlayer(context, "win"),
            lose = tryCreatePlayer(context, "lose")
        )
    }
    DisposableEffect(Unit) { onDispose { sfx.release() } }

    var bet by rememberSaveable { mutableStateOf("100") }
    val betInt = bet.toIntOrNull() ?: 0
    val canStart = betInt in 1..balance
    val isGameInProgress = uiState.isPlayerTurn

    var outcomeText by remember { mutableStateOf<String?>(null) }
    var outcomePositive by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    // Reacción a mensajes (Lógica original intacta)
    LaunchedEffect(isGameInProgress, uiState.gameMessage) {
        if (!isGameInProgress && uiState.playerHand.isNotEmpty()) sfx.flip.startSafely()
        uiState.gameMessage?.let { msg ->
            outcomeText = msg
            when {
                msg.contains("Ganaste", true) || msg.contains("Blackjack", true) -> {
                    outcomePositive = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    sfx.win.startSafely()
                    showConfetti = true
                    launch { delay(3800); showConfetti = false }
                }
                msg.contains("Perdiste", true) -> {
                    outcomePositive = false
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    sfx.lose.startSafely()
                }
                else -> { outcomePositive = false }
            }
            launch { delay(4200); outcomeText = null }
        }
    }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { GameHeader(imageRes = R.drawable.blackjack_background, balance = balance) }

            // --- MESA DE JUEGO (PAÑO VERDE) ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp) // Altura fija para la "mesa"
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(TableGradient)
                        .border(6.dp, Color(0xFF3E2723), RoundedCornerShape(32.dp)) // Borde madera
                        .border(2.dp, GoldBorder, RoundedCornerShape(32.dp)) // Filete dorado interior
                ) {
                    // Marca de agua en la mesa
                    Text(
                        "BLACKJACK PAYS 3 TO 2",
                        color = Color.White.copy(alpha = 0.1f),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center).rotate(-15f),
                        style = MaterialTheme.typography.displaySmall
                    )

                    Column(
                        Modifier.fillMaxSize().padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // CRUPIER
                        if (uiState.playerHand.isNotEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("CRUPIER", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(Modifier.height(8.dp))
                                CardsRow(
                                    cards = uiState.dealerHand,
                                    dealer = true,
                                    hiddenSecond = isGameInProgress
                                )
                                Spacer(Modifier.height(4.dp))
                                if (!isGameInProgress) {
                                    TotalBadge(handTotal(uiState.dealerHand))
                                }
                            }
                        }

                        // JUGADOR
                        if (uiState.playerHand.isNotEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                TotalBadge(handTotal(uiState.playerHand))
                                Spacer(Modifier.height(4.dp))
                                CardsRow(cards = uiState.playerHand)
                                Spacer(Modifier.height(8.dp))
                                Text("TÚ", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else {
                            // Mensaje de espera
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Haz tu apuesta para jugar", color = Color.White.copy(0.6f))
                            }
                        }
                    }

                    // Mensaje flotante dentro de la mesa
                    uiState.gameMessage?.let {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .background(Color.Black.copy(0.7f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(it, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- CONTROLES ---
            item {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (isGameInProgress) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); sfx.flip.startSafely(); onHit() },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)) // Azul
                                ) { Text("PEDIR CARTA") }
                                Button(
                                    onClick = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onStand() },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)) // Rojo
                                ) { Text("PLANTARSE") }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = bet,
                                    onValueChange = { bet = it.filter(Char::isDigit) },
                                    label = { Text("Apuesta") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Button(
                                    onClick = { sfx.shuffle.startSafely(); onStartGame(betInt) },
                                    enabled = canStart,
                                    modifier = Modifier.height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark)
                                ) { Text("REPARTIR", color = Color.Black, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = outcomeText != null) {
                    ResultBanner(text = outcomeText ?: "", positive = outcomePositive)
                }
            }
        }
        ConfettiOverlay(visible = showConfetti)
    }
}

// --- COMPONENTES VISUALES VIP ---

@Composable
private fun CardsRow(cards: List<Int>, dealer: Boolean = false, hiddenSecond: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-30).dp), // Efecto de superposición (fan)
        modifier = Modifier.height(100.dp)
    ) {
        if (cards.isEmpty()) return
        cards.forEachIndexed { index, card ->
            if (dealer && index == 1 && hiddenSecond) {
                HiddenCardView()
            } else {
                CardView(cardValueToString(card))
            }
        }
    }
}

@Composable
private fun CardView(value: String) {
    // Carta física realista
    Surface(
        modifier = Modifier
            .size(70.dp, 100.dp)
            .shadow(4.dp, RoundedCornerShape(6.dp))
            .rotate(Random.nextInt(-3, 3).toFloat()), // Leve rotación natural
        shape = RoundedCornerShape(6.dp),
        color = CardWhite,
        border = BorderStroke(1.dp, Color.Gray.copy(0.3f))
    ) {
        Box(Modifier.fillMaxSize()) {
            Text(
                text = value,
                color = if (Random.nextBoolean()) Color.Black else Color(0xFFB71C1C), // Simula palos rojo/negro
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(6.dp).align(Alignment.TopStart)
            )
            Text(
                text = value,
                color = Color.Black.copy(0.2f),
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = value,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(6.dp).align(Alignment.BottomEnd).rotate(180f)
            )
        }
    }
}

@Composable
private fun HiddenCardView() {
    Box(
        modifier = Modifier
            .size(70.dp, 100.dp)
            .shadow(4.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFB71C1C), Color(0xFF8B0000)))) // Dorso rojo clásico
            .border(2.dp, Color.White, RoundedCornerShape(6.dp))
    ) {
        // Patrón en el dorso
        Box(Modifier.align(Alignment.Center).size(40.dp).background(Color.White.copy(0.1f), CircleShape))
    }
}

@Composable
private fun TotalBadge(total: Int) {
    Surface(
        color = Color.Black.copy(0.6f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, GoldAccent)
    ) {
        Text(
            text = "$total",
            color = GoldAccent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            fontSize = 12.sp
        )
    }
}

// Helpers copiados del original para mantener la lógica funcionando
private fun cardValueToString(value: Int) = when (value) { 11 -> "A"; 10 -> listOf("10","J","Q","K").random(); else -> value.toString() }
private fun handTotal(cards: List<Int>): Int {
    var total = cards.sum()
    var aces = cards.count { it == 11 }
    while (total > 21 && aces > 0) { total -= 10; aces-- }
    return total
}

// Confetti y SFX Utils (Mantenidos del original, resumidos para copy-paste)
@Composable private fun ConfettiOverlay(visible: Boolean) { /* ... Código previo ... */ }
private data class SoundFx(val shuffle: MediaPlayer?, val flip: MediaPlayer?, val win: MediaPlayer?, val lose: MediaPlayer?) { fun release() { listOf(shuffle, flip, win, lose).forEach { it?.release() } } }
private fun tryCreatePlayer(c: android.content.Context, n: String): MediaPlayer? = try { val id = c.resources.getIdentifier(n, "raw", c.packageName); if (id==0) null else MediaPlayer.create(c, id) } catch(_:Throwable){null}
private fun MediaPlayer?.startSafely() { try { this?.let { it.seekTo(0); it.start() } } catch (_:Throwable){} }