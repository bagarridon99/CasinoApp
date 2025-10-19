@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.casinoapp.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.R
import com.example.casinoapp.model.CasinoUiState
import com.example.casinoapp.model.RouletteBet
import com.example.casinoapp.model.RouletteColor
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

/* ------------------------------ Tipos/estado ------------------------------ */

private enum class BetMode { Color, Numero }

private data class Outcome(val positive: Boolean, val wonAmount: Int, val lostAmount: Int)

/* -------------------------------- Pantalla -------------------------------- */

@Composable
fun RouletteScreen(
    uiState: CasinoUiState,
    onPlay: (Int, RouletteBet) -> Unit,
    modifier: Modifier = Modifier
) {
    var betAmount by rememberSaveable { mutableStateOf("50") }
    var betMode by rememberSaveable { mutableStateOf(BetMode.Color) }
    var selectedBet by remember { mutableStateOf<RouletteBet>(RouletteBet.ByColor(RouletteColor.ROJO)) }

    // Resultado del último giro (para la banda)
    var lastOutcome by remember { mutableStateOf<Outcome?>(null) }
    var showOutcome by remember { mutableStateOf(false) }

    val amountInt = betAmount.toIntOrNull() ?: 0
    val canBet = amountInt in 1..uiState.balance &&
            when (betMode) {
                BetMode.Color -> selectedBet is RouletteBet.ByColor
                BetMode.Numero -> (selectedBet as? RouletteBet.ByNumber)?.number in 0..36
            }

    // Cuando aparezca un número ganador, calculamos si esta selección ganó o perdió
    val winningNumber = uiState.rouletteState.winningNumber
    LaunchedEffect(winningNumber) {
        if (winningNumber != null && amountInt > 0) {
            val payoutTotal = calcPayout(amountInt, selectedBet, winningNumber) // total (apuesta + ganancia)
            lastOutcome = if (payoutTotal > 0) {
                Outcome(positive = true, wonAmount = payoutTotal, lostAmount = 0)
            } else {
                Outcome(positive = false, wonAmount = 0, lostAmount = amountInt)
            }
            showOutcome = true
        }
    }

    // Auto-ocultar la banda de resultado
    LaunchedEffect(showOutcome) {
        if (showOutcome) {
            delay(4000)
            showOutcome = false
        }
    }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { GameHeader(imageRes = R.drawable.roulette_background, balance = uiState.balance) }

            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f)
                    )
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Monto
                        OutlinedTextField(
                            value = betAmount,
                            onValueChange = { betAmount = it.filter(Char::isDigit) },
                            label = { Text("Monto de la apuesta") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Presets rápidos
                        QuickAmountChips(
                            onPick = { picked -> betAmount = picked.toString() },
                            balance = uiState.balance
                        )

                        // Selector de modo (Color / Número)
                        TabRow(selectedTabIndex = betMode.ordinal) {
                            BetMode.values().forEach { mode ->
                                Tab(
                                    selected = betMode == mode,
                                    onClick = {
                                        betMode = mode
                                        selectedBet =
                                            if (mode == BetMode.Color) RouletteBet.ByColor(RouletteColor.ROJO)
                                            else RouletteBet.ByNumber(1)
                                    },
                                    text = { Text(if (mode == BetMode.Color) "Color" else "Número") }
                                )
                            }
                        }

                        // Contenido según modo
                        when (betMode) {
                            BetMode.Color -> ColorBetSelector(
                                currentBet = selectedBet,
                                onBetSelected = { selectedBet = it }
                            )

                            BetMode.Numero -> NumberBetSelector(
                                currentBet = selectedBet,
                                onBetSelected = { selectedBet = it }
                            )
                        }

                        // Botón jugar
                        Button(
                            onClick = { onPlay(amountInt, selectedBet) },
                            enabled = canBet,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) { Text("Jugar ruleta") }

                        // Pagos
                        PayoutLegend()
                    }
                }
            }

            // Número ganador centrado
            item { WinningNumberDisplay(uiState.rouletteState.winningNumber) }

            // Banda de resultado (gana / pierde)
            item {
                if (showOutcome) ResultBanner(lastOutcome)
            }
        }
    }
}

/* --------------------------------- Header -------------------------------- */

@Composable
private fun GameHeader(imageRes: Int, balance: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Overlay para contraste
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
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

/* ------------------------- Selector de Color ------------------------- */

@Composable
private fun ColorBetSelector(
    currentBet: RouletteBet,
    onBetSelected: (RouletteBet.ByColor) -> Unit
) {
    val selected = (currentBet as? RouletteBet.ByColor)?.color
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RouletteColor.values().forEach { color ->
            val selectedBorder =
                if (selected == color) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            OutlinedButton(
                onClick = { onBetSelected(RouletteBet.ByColor(color)) },
                modifier = Modifier.weight(1f),
                border = selectedBorder
            ) { Text(color.label) }
        }
    }
}

/* ------------------------- Selector de Número ------------------------- */

@Composable
private fun NumberBetSelector(
    currentBet: RouletteBet,
    onBetSelected: (RouletteBet.ByNumber) -> Unit
) {
    val selectedNumber = (currentBet as? RouletteBet.ByNumber)?.number
    val numbers = (0..36).toList()

    Text("Selecciona un número", style = MaterialTheme.typography.titleMedium)
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(numbers) { number ->
            val color = getRouletteNumberColor(number)
            val isSelected = selectedNumber == number
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else color)
                    .clickable { onBetSelected(RouletteBet.ByNumber(number)) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    number.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* --------------------- Número ganador (centrado) --------------------- */

@Composable
private fun WinningNumberDisplay(winningNumber: Int?) {
    if (winningNumber == null) {
        Card(
            Modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("¡Haz tu apuesta!", style = MaterialTheme.typography.headlineSmall)
            }
        }
    } else {
        val resultColor = getRouletteNumberColor(winningNumber)
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Número ganador", style = MaterialTheme.typography.titleMedium)
                    Box(
                        Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(resultColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$winningNumber",
                            fontSize = 50.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/* --------------------------- Banda de resultado --------------------------- */

@Composable
private fun ResultBanner(outcome: Outcome?) {
    if (outcome == null) return

    val positive = outcome.positive
    val bg = if (positive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val fg = if (positive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    val text = if (positive)
        "¡Ganaste ${formatCLP(outcome.wonAmount)}!"
    else
        "Perdiste ${formatCLP(outcome.lostAmount)}"

    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ------------------------------- Leyenda pagos ---------------------------- */

@Composable
private fun PayoutLegend() {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Pagos:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
        Text("• Color (Rojo/Negro) 1:1", style = MaterialTheme.typography.bodySmall)
        Text("• Verde 17:1", style = MaterialTheme.typography.bodySmall)
        Text("• Número exacto 35:1", style = MaterialTheme.typography.bodySmall)
    }
}

/* ----------------------------- Chips de montos ---------------------------- */

@Composable
private fun QuickAmountChips(onPick: (Int) -> Unit, balance: Int) {
    val presets = listOf(50, 100, 200, 500)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        presets.forEach { v ->
            AssistChip(onClick = { onPick(v) }, label = { Text("\$${v}") })
        }
        AssistChip(onClick = { onPick(balance.coerceAtLeast(1)) }, label = { Text("MAX") })
    }
}

/* --------------------------------- Helpers -------------------------------- */

private fun getRouletteNumberColor(number: Int): Color =
    when {
        number == 0 -> Color(0xFF008000) // verde
        number in setOf(
            1, 3, 5, 7, 9, 12, 14, 16, 18,
            19, 21, 23, 25, 27, 30, 32, 34, 36
        ) -> Color(0xFFB71C1C) // rojo
        else -> Color.Black
    }

/**
 * Devuelve el **total** a pagar (apuesta + ganancia) si gana; 0 si pierde.
 * - Color Rojo/Negro: 1:1  -> total = amount * 2
 * - Verde: 17:1            -> total = amount * 18
 * - Número exacto: 35:1    -> total = amount * 36
 */
private fun calcPayout(amount: Int, bet: RouletteBet, winningNumber: Int): Int {
    return when (bet) {
        is RouletteBet.ByColor -> {
            val colorWin = when (bet.color) {
                RouletteColor.ROJO -> winningNumber != 0 && getRouletteNumberColor(winningNumber) == Color(0xFFB71C1C)
                RouletteColor.NEGRO -> winningNumber != 0 && getRouletteNumberColor(winningNumber) == Color.Black
                RouletteColor.VERDE -> winningNumber == 0
            }
            if (!colorWin) 0 else {
                if (bet.color == RouletteColor.VERDE) amount * 18 else amount * 2
            }
        }
        is RouletteBet.ByNumber -> if (bet.number == winningNumber) amount * 36 else 0
    }
}

private fun formatCLP(value: Int): String =
    NumberFormat.getCurrencyInstance(Locale("es", "CL")).format(value)
