package com.example.casinoapp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.R
import com.example.casinoapp.model.CasinoUiState

@Composable
fun SlotsScreen(
    uiState: CasinoUiState,
    onPlay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var bet by rememberSaveable { mutableStateOf("25") }
    val betInt = bet.toIntOrNull() ?: 0
    val canSpin = betInt in 1..uiState.balance

    // Estado local para “girando” y resultado mostrado
    var spinning by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var resultWin by remember { mutableStateOf(false) }

    // Cuando cambien los resultados desde el VM, dejamos de “girar” y calculamos el resultado
    LaunchedEffect(uiState.slotResults) {
        if (spinning) {
            spinning = false
        }
        if (uiState.slotResults.isNotEmpty() && betInt > 0) {
            val payout = calcSlotsPayout(betInt, uiState.slotResults.map { it.emoji })
            resultWin = payout > 0
            resultText = if (resultWin) {
                "¡Ganaste ${formatCLP(payout)}!"
            } else {
                "Perdiste ${formatCLP(betInt)}"
            }
        }
    }

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
                modifier = Modifier
                    .padding(horizontal = 16.dp),
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
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (!canSpin) {
                                Text(
                                    "Debe ser mayor a 0 y no superar tu saldo.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    Button(
                        onClick = {
                            resultText = null       // limpiamos resultado anterior
                            spinning = true
                            onPlay(betInt)
                        },
                        enabled = canSpin && !spinning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (spinning) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Girando…")
                        } else {
                            Text("Girar")
                        }
                    }

                    PayoutLegend()
                }
            }

            // Rodillos
            if (uiState.slotResults.isNotEmpty() || spinning) {
                ElevatedCard(
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
                        if (spinning) {
                            // Estado simple mientras “gira”
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val placeholder = listOf("🎰", "🎲", "⭐")
                            val symbols = if (spinning) placeholder else uiState.slotResults.map { it.emoji }
                            // 3 “ventanas” del slot
                            symbols
                                .padEndTo(3, "⭐") // por si el VM trae menos de 3 temporalmente
                                .take(3)
                                .forEach { emoji ->
                                    ReelWindow(emoji)
                                }
                        }

                        // Resultado (ganaste/perdiste)
                        resultText?.let { txt ->
                            Spacer(Modifier.height(12.dp))
                            ResultBanner(text = txt, positive = resultWin)
                        }
                    }
                }
            } else {
                // Estado vacío
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "¡Gira para jugar!",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/* ------------------------------- UI parts ------------------------------- */

@Composable
private fun ReelWindow(emoji: String) {
    Surface(
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .widthIn(min = 88.dp)
            .heightIn(min = 88.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                emoji,
                fontSize = 48.sp,
                lineHeight = 48.sp
            )
        }
    }
}

@Composable
private fun ResultBanner(text: String, positive: Boolean) {
    Surface(
        color = if (positive) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun PayoutLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            "Premios:",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Text("• 3 iguales → x4", style = MaterialTheme.typography.bodySmall)
        Text("• 2 iguales → x2", style = MaterialTheme.typography.bodySmall)
        Text("• Si no, pierdes tu apuesta", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun GameHeader(imageRes: Int, balance: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Box {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))
            Text(
                "Saldo: ${formatCLP(balance)}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

/* -------------------------------- Logic -------------------------------- */

private fun calcSlotsPayout(bet: Int, emojis: List<String>): Int {
    if (emojis.size < 3) return 0
    val a = emojis[0]; val b = emojis[1]; val c = emojis[2]
    return when {
        a == b && b == c -> bet * 4      // 3 iguales
        a == b || a == c || b == c -> bet * 2 // 2 iguales
        else -> 0
    }
}

private fun List<String>.padEndTo(size: Int, pad: String): List<String> {
    if (this.size >= size) return this
    val out = this.toMutableList()
    repeat(size - this.size) { out.add(pad) }
    return out
}

private fun formatCLP(value: Int): String {
    val nf = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("es", "CL"))
    return nf.format(value)
}
