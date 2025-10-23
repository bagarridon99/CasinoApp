package com.example.casinoapp.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.casinoapp.R
import com.example.casinoapp.model.CasinoUiState
import com.example.casinoapp.model.SlotSymbol
import com.example.casinoapp.ui.common.GameHeader
import com.example.casinoapp.ui.common.ResultBanner
import com.example.casinoapp.ui.common.formatCLP
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.roundToInt
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun SlotsScreen(
    uiState: CasinoUiState,
    onPlay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var bet by rememberSaveable { mutableStateOf("25") }
    val betInt = bet.toIntOrNull() ?: 0
    val canSpin = betInt in 1..uiState.balance

    // ==== Estado de animación nivel 1 ====
    val scope = rememberCoroutineScope()
    val symbols = remember { SlotSymbol.values().map { it.emoji } }
    val n = symbols.size

    // índice "virtual" por rodillo (se mueve en float y se dibuja con floor % n)
    val reel1 = remember { Animatable(0f) }
    val reel2 = remember { Animatable(0f) }
    val reel3 = remember { Animatable(0f) }

    // control de giro y resultado
    var spinning by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var resultPositive by remember { mutableStateOf(false) }
    var pendingResult: List<String>? by remember { mutableStateOf(null) }

    // Para cancelar animaciones si el usuario sale
    var spinJob1: Job? by remember { mutableStateOf(null) }
    var spinJob2: Job? by remember { mutableStateOf(null) }
    var spinJob3: Job? by remember { mutableStateOf(null) }

    // Cuando el VM entrega resultados, los guardamos como "pendientes" si estamos girando
    LaunchedEffect(uiState.slotResults) {
        if (spinning && uiState.slotResults.isNotEmpty()) {
            pendingResult = uiState.slotResults.map { it.emoji }
        }
        if (!spinning && uiState.slotResults.isNotEmpty() && betInt > 0) {
            // (fallback) si por alguna razón no estábamos en spin, mostramos copy final
            val payout = calcSlotsPayout(betInt, uiState.slotResults.map { it.emoji }, symbols)
            resultPositive = payout > 0
            resultText = if (resultPositive) {
                "¡Ganaste ${formatCLP(payout)}!"
            } else {
                "Perdiste ${formatCLP(betInt)}"
            }
        }
    }

    // Layout raíz
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GameHeader(imageRes = R.drawable.slots_background, balance = uiState.balance)

            // Panel de apuesta + botón
            Card(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = bet,
                        onValueChange = { bet = it.filter(Char::isDigit) },
                        label = { Text("Monto de la apuesta") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            resultText = null
                            resultPositive = false
                            pendingResult = null
                            spinning = true

                            // Dispara animaciones falsas (rápidas) con frenado escalonado,
                            // luego "snap" al resultado real cuando esté disponible.
                            spinJob1?.cancel(); spinJob2?.cancel(); spinJob3?.cancel()
                            spinJob1 = scope.launch { spinReel(reel1, baseLoops = 12f, n = n, waitFor = 0L) }
                            spinJob2 = scope.launch { spinReel(reel2, baseLoops = 14f, n = n, waitFor = 150L) }
                            spinJob3 = scope.launch { spinReel(reel3, baseLoops = 16f, n = n, waitFor = 300L) }

                            // Llama al juego real
                            onPlay(betInt)

                            // Coordina la fase final: espera resultado y hace snap (1->2->3 con desfase)
                            scope.launch {
                                // Espera a que llegue el resultado del VM
                                while (pendingResult == null) delay(30)
                                val target = pendingResult!!

                                // Frenado + snap por rodillo con leve delay
                                snapReelToResult(reel1, symbols, target[0])
                                delay(150)
                                snapReelToResult(reel2, symbols, target[1 % target.size])
                                delay(150)
                                snapReelToResult(reel3, symbols, target[2 % target.size])

                                // Mostrar banner final una vez alineados los 3
                                val payout = calcSlotsPayout(betInt, target, symbols)
                                resultPositive = payout > 0
                                resultText = if (resultPositive) {
                                    "¡Ganaste ${formatCLP(payout)}!"
                                } else {
                                    "Perdiste ${formatCLP(betInt)}"
                                }
                                spinning = false
                            }
                        },
                        enabled = canSpin && !spinning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    ) {
                        if (spinning) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.size(10.dp))
                            Text("Girando…")
                        } else {
                            Text("Girar")
                        }
                    }

                    PayoutLegend()
                }
            }

            // Rodillos animados
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReelWindow(symbols = symbols, anim = reel1)
                        ReelWindow(symbols = symbols, anim = reel2)
                        ReelWindow(symbols = symbols, anim = reel3)
                    }

                    AnimatedVisibility(visible = resultText != null) {
                        Column(Modifier.padding(top = 12.dp)) {
                            ResultBanner(text = resultText.orEmpty(), positive = resultPositive)
                        }
                    }
                }
            }
        }
    }
}

/* ============================================================
 * Animación "Nivel 1": rápido -> freno -> snap al resultado
 * ============================================================ */

private suspend fun spinReel(
    anim: Animatable<Float, *>,
    baseLoops: Float,
    n: Int,
    waitFor: Long
) {
    // cada rodillo parte con un pequeño delay para dar efecto cascada
    if (waitFor > 0) delay(waitFor)

    // Acelera lineal y da ~ baseLoops vueltas
    val start = anim.value
    val target = start + baseLoops
    anim.animateTo(
        targetValue = target,
        animationSpec = tween(durationMillis = 700, easing = LinearEasing)
    )

    // Pequeño tramo extra para que quede "en curso" hasta el snap final
    anim.animateTo(
        targetValue = anim.value + (n / 2f),
        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
    )
}

private suspend fun snapReelToResult(
    anim: Animatable<Float, *>,
    symbols: List<String>,
    targetEmoji: String
) {
    val n = symbols.size
    val current = anim.value
    val atIndex = (floor(current).toInt() % n + n) % n
    val targetIndex = symbols.indexOf(targetEmoji).takeIf { it >= 0 } ?: 0

    // pasos hacia adelante (mínimo 1 vuelta extra para sensación de "enganche")
    val forwardSteps = ((targetIndex - atIndex + n) % n) + n
    val finalTarget = current + forwardSteps.toFloat()

    anim.animateTo(
        targetValue = finalTarget,
        animationSpec = tween(durationMillis = 550, easing = LinearEasing)
    )
}

/* ============================================================
 * UI de un "ventanal" de rodillo (muestra el símbolo actual)
 * ============================================================ */

@Composable
private fun ReelWindow(
    symbols: List<String>,
    anim: Animatable<Float, *>,
) {
    // índice del símbolo a mostrar
    val n = symbols.size
    val currentIndex = ((floor(anim.value).toInt() % n) + n) % n
    val emoji = symbols[currentIndex]

    Box(
        modifier = Modifier
            .widthIn(min = 88.dp)
            .heightIn(min = 88.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.displaySmall // grande y vistoso
        )
    }
}

/* ============================================================
 * Payout y leyenda simple (compat con tu UI previa)
 * ============================================================ */

private fun calcSlotsPayout(bet: Int, result: List<String>, all: List<String>): Int {
    // payout simple: 3 iguales = 5x; 2 iguales = 2x; si no, pierdes la apuesta
    // (Puedes reemplazarlo por tu cálculo real si difiere)
    if (result.size < 3) return -bet
    val a = result[0]; val b = result[1]; val c = result[2]
    return when {
        a == b && b == c -> bet * 5
        a == b || b == c || a == c -> bet * 2
        else -> -bet
    }
}

@Composable
private fun PayoutLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Pagos:", style = MaterialTheme.typography.titleMedium)
        Text("• 3 iguales = x5", style = MaterialTheme.typography.bodySmall)
        Text("• 2 iguales = x2", style = MaterialTheme.typography.bodySmall)
    }
}
