@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.casinoapp.view

import android.app.Application
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.R
import com.example.casinoapp.model.CasinoUiState
import com.example.casinoapp.model.SlotSymbol
import com.example.casinoapp.ui.common.GameHeader
import com.example.casinoapp.ui.common.formatCLP
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor

// --- Colores Especiales ---
val MetalGold = Color(0xFFFFD700)
val DarkGold = Color(0xFFC5A000)
val GoldGradient = Brush.verticalGradient(listOf(DarkGold, MetalGold, DarkGold))
val ReelGradient = Brush.verticalGradient(
    0.0f to Color.Black.copy(alpha = 0.6f),
    0.2f to Color.Transparent,
    0.8f to Color.Transparent,
    1.0f to Color.Black.copy(alpha = 0.6f)
)

@Composable
fun SlotsScreen(
    uiState: CasinoUiState,
    onPlay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var bet by rememberSaveable { mutableStateOf("100") }
    val betInt = bet.toIntOrNull() ?: 0
    val canSpin = betInt in 1..uiState.balance

    val scope = rememberCoroutineScope()
    val symbols = remember { SlotSymbol.values().map { it.emoji } }
    val n = symbols.size

    val reel1 = remember { Animatable(0f) }
    val reel2 = remember { Animatable(0f) }
    val reel3 = remember { Animatable(0f) }

    var spinning by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var resultPositive by remember { mutableStateOf(false) }
    var showOutcomeBanner by remember { mutableStateOf(false) } // NUEVO
    var pendingResult: List<String>? by remember { mutableStateOf(null) }

    var spinJob1: Job? by remember { mutableStateOf(null) }
    var spinJob2: Job? by remember { mutableStateOf(null) }
    var spinJob3: Job? by remember { mutableStateOf(null) }

    // --- MEJORA DE COLORES PARA INPUTS (Visibilidad Blanca) ---
    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MetalGold,
        unfocusedBorderColor = Color.Gray,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color.Black.copy(0.8f),
        unfocusedContainerColor = Color.Black.copy(0.6f),
        focusedLabelColor = MetalGold,
        unfocusedLabelColor = Color.White
    )

    // Detección de resultado
    LaunchedEffect(uiState.slotResults) {
        if (spinning && uiState.slotResults.isNotEmpty()) {
            pendingResult = uiState.slotResults.map { it.emoji }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { GameHeader(imageRes = R.drawable.slots_background, balance = uiState.balance) }

            // --- LA MÁQUINA PRINCIPAL ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF222222))
                        .border(4.dp, GoldGradient, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "SUPER SLOTS",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = MetalGold
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .background(Color.Black, RoundedCornerShape(8.dp))
                                .border(2.dp, Color(0xFF444444), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReelWindow(symbols, reel1)
                            Box(Modifier.width(2.dp).fillMaxHeight().background(DarkGold))
                            ReelWindow(symbols, reel2)
                            Box(Modifier.width(2.dp).fillMaxHeight().background(DarkGold))
                            ReelWindow(symbols, reel3)
                        }
                    }
                }
            }

            // --- CONTROLES DE APUESTA ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.4f))
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = bet,
                            onValueChange = { bet = it.filter(Char::isDigit) },
                            label = { Text("Apuesta") },
                            colors = tfColors,
                            textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                resultText = null; resultPositive = false; pendingResult = null; spinning = true
                                spinJob1?.cancel(); spinJob2?.cancel(); spinJob3?.cancel()

                                spinJob1 = scope.launch { spinReel(reel1, 12f, n, 0L) }
                                spinJob2 = scope.launch { spinReel(reel2, 14f, n, 150L) }
                                spinJob3 = scope.launch { spinReel(reel3, 16f, n, 300L) }

                                onPlay(betInt)

                                scope.launch {
                                    while (pendingResult == null) delay(50)
                                    val target = pendingResult!!
                                    snapReelToResult(reel1, symbols, target[0])
                                    delay(100)
                                    snapReelToResult(reel2, symbols, target[1 % n])
                                    delay(100)
                                    snapReelToResult(reel3, symbols, target[2 % n])

                                    val payout = calcSlotsPayout(betInt, target, symbols)
                                    resultPositive = payout > 0
                                    resultText = if (resultPositive) "¡GANASTE ${formatCLP(payout)}!" else "PERDISTE ${formatCLP(betInt)}"

                                    spinning = false
                                    showOutcomeBanner = true
                                    delay(1000) // DURACIÓN 1 SEGUNDO
                                    showOutcomeBanner = false
                                }
                            },
                            enabled = canSpin && !spinning,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (canSpin) Color(0xFFD32F2F) else Color.Gray),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            if (spinning) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            else Text("¡GIRAR!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        PayoutLegend()
                    }
                }
            }
        }

        // --- ANUNCIO GANAR/PERDER: TOTALMENTE CENTRADO ---
        AnimatedVisibility(
            visible = showOutcomeBanner && resultText != null,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (resultPositive) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                ),
                elevation = CardDefaults.cardElevation(16.dp),
                border = BorderStroke(2.dp, MetalGold)
            ) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = resultText?.uppercase() ?: "",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

// --- COMPONENTES VISUALES ---

@Composable
private fun ReelWindow(symbols: List<String>, anim: Animatable<Float, *>) {
    val n = symbols.size
    val currentIndex = ((floor(anim.value).toInt() % n) + n) % n
    val emoji = symbols[currentIndex]

    Box(
        modifier = Modifier
            .width(80.dp)
            .height(110.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 48.sp)
        Box(modifier = Modifier.fillMaxSize().background(ReelGradient))
    }
}

@Composable
private fun PayoutLegend() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("🍒🍒🍒 = x3", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.6f))
        Text("💎💎💎 = x6", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.6f))
        Text("7️⃣7️⃣7️⃣ = x10", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.6f))
    }
}

// --- LÓGICA DE ANIMACIÓN ---
private suspend fun spinReel(anim: Animatable<Float, *>, baseLoops: Float, n: Int, waitFor: Long) {
    if (waitFor > 0) delay(waitFor)
    anim.animateTo(anim.value + baseLoops, animationSpec = tween(700, easing = LinearEasing))
    anim.animateTo(anim.value + (n / 2f), animationSpec = tween(500, easing = LinearEasing))
}

private suspend fun snapReelToResult(anim: Animatable<Float, *>, symbols: List<String>, targetEmoji: String) {
    val n = symbols.size
    val current = anim.value
    val atIndex = (floor(current).toInt() % n + n) % n
    val targetIndex = symbols.indexOf(targetEmoji).takeIf { it >= 0 } ?: 0
    val forwardSteps = ((targetIndex - atIndex + n) % n) + n
    anim.animateTo(current + forwardSteps.toFloat(), animationSpec = tween(600, easing = LinearEasing))
}

private fun calcSlotsPayout(bet: Int, result: List<String>, all: List<String>): Int {
    if (result.size < 3) return -bet
    val a = result[0]; val b = result[1]; val c = result[2]
    return when {
        a == b && b == c -> bet * when(a) {
            "7️⃣" -> 10; "💎" -> 6; "🍀" -> 5; "🔔" -> 4; else -> 3
        }
        a == b || b == c || a == c -> bet * 2
        else -> -bet
    }
}