@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.casinoapp.view

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.R
import com.example.casinoapp.model.CasinoUiState
import com.example.casinoapp.model.RouletteBet
import com.example.casinoapp.model.RouletteColor
import com.example.casinoapp.ui.common.GameHeader
import com.example.casinoapp.ui.common.ResultBanner
import com.example.casinoapp.ui.common.formatCLP
import com.example.casinoapp.ui.common.RouletteWheel
import kotlinx.coroutines.delay
import com.example.casinoapp.ui.common.RouletteWheel
import com.example.casinoapp.ui.common.EURO_WHEEL_ORDER
import com.example.casinoapp.ui.common.SLOT_DEG
import com.example.casinoapp.ui.common.angleForNumber
import com.example.casinoapp.ui.common.forwardDelta
import com.example.casinoapp.ui.common.norm360


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

    var lastOutcome by remember { mutableStateOf<Outcome?>(null) }
    var showOutcome by remember { mutableStateOf(false) }
    var spinCount by remember { mutableStateOf(0) }

    val amountInt = betAmount.toIntOrNull() ?: 0
    val canBet = amountInt in 1..uiState.balance &&
            when (betMode) {
                BetMode.Color -> selectedBet is RouletteBet.ByColor
                BetMode.Numero -> (selectedBet as? RouletteBet.ByNumber)?.number in 0..36
            }

    val winningNumber = uiState.rouletteState.winningNumber

    // Cuando hay número ganador, calcula outcome
    LaunchedEffect(winningNumber) {
        if (winningNumber != null && amountInt > 0) {
            val payoutTotal = calcPayout(amountInt, selectedBet, winningNumber)
            lastOutcome = if (payoutTotal > 0) {
                Outcome(true, payoutTotal, 0)
            } else {
                Outcome(false, 0, amountInt)
            }
            showOutcome = true
        }
    }

    // Oculta banner solo
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
            item {
                GameHeader(imageRes = R.drawable.roulette_background, balance = uiState.balance)
            }

            item {
                RouletteWheel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(300.dp),
                    size = 260.dp,
                    targetNumber = winningNumber,
                    spinTrigger = spinCount
                )
            }

            // Etiqueta con el número salido + color (si hay)
            item {
                WinningNumberLabel(
                    number = winningNumber
                )
            }

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
                            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Chips inline
                        val presets = listOf(50, 100, 200, 500)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presets.forEach { v ->
                                AssistChip(onClick = { betAmount = v.toString() }, label = { Text("\$${v}") })
                            }
                            AssistChip(
                                onClick = { betAmount = uiState.balance.coerceAtLeast(1).toString() },
                                label = { Text("MAX") }
                            )
                        }

                        // Tabs (Color / Número)
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
                            BetMode.Color -> ColorBetSelectorBlock(
                                current = selectedBet,
                                onBetSelected = { selectedBet = it }
                            )
                            BetMode.Numero -> NumberBetSelectorBlock(
                                current = selectedBet,
                                onBetSelected = { selectedBet = it }
                            )
                        }

                        // Botón jugar
                        Button(
                            onClick = {
                                onPlay(amountInt, selectedBet) // VM decide el número ganador
                                spinCount += 1                 // dispara animación
                            },
                            enabled = canBet,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) { Text("Jugar ruleta") }

                        // Pagos
                        PayoutLegendBlock()
                    }
                }
            }

            // Banner con monto + número salido
            item {
                if (showOutcome && winningNumber != null) {
                    val colorName = rouletteColorName(winningNumber)
                    ResultBanner(
                        text = if (lastOutcome?.positive == true)
                            "Salió $winningNumber ($colorName) — ¡Ganaste ${formatCLP(lastOutcome?.wonAmount ?: 0)}!"
                        else
                            "Salió $winningNumber ($colorName) — Perdiste ${formatCLP(lastOutcome?.lostAmount ?: 0)}",
                        positive = lastOutcome?.positive == true
                    )
                }
            }
        }
    }
}

/* ------------------------- Selector de Color (nombre único) ------------------------- */
@Composable
private fun ColorBetSelectorBlock(
    current: RouletteBet,
    onBetSelected: (RouletteBet.ByColor) -> Unit
) {
    val selected = (current as? RouletteBet.ByColor)?.color
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

/* ------------------------- Selector de Número (nombre único) ------------------------ */
@Composable
private fun NumberBetSelectorBlock(
    current: RouletteBet,
    onBetSelected: (RouletteBet.ByNumber) -> Unit
) {
    val selectedNumber = (current as? RouletteBet.ByNumber)?.number
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
                    fontSize = 14.sp
                )
            }
        }
    }
}

/* ------------------------------ Número + color ----------------------------- */
@Composable
private fun WinningNumberLabel(number: Int?) {
    if (number == null) {
        Card(
            Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("¡Haz tu apuesta!", style = MaterialTheme.typography.titleMedium)
            }
        }
    } else {
        val color = getRouletteNumberColor(number)
        val name = rouletteColorName(number)
        Card(
            Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "$number",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = "Salió $number ($name)",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

/* ------------------------------- Leyenda pagos ---------------------------- */
@Composable
private fun PayoutLegendBlock() {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Pagos:", style = MaterialTheme.typography.bodySmall)
        Text("• Color (Rojo/Negro) 1:1", style = MaterialTheme.typography.bodySmall)
        Text("• Verde 17:1", style = MaterialTheme.typography.bodySmall)
        Text("• Número exacto 35:1", style = MaterialTheme.typography.bodySmall)
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

private fun rouletteColorName(number: Int): String =
    when {
        number == 0 -> "Verde"
        number in setOf(
            1, 3, 5, 7, 9, 12, 14, 16, 18,
            19, 21, 23, 25, 27, 30, 32, 34, 36
        ) -> "Rojo"
        else -> "Negro"
    }

/** Total a pagar (apuesta + ganancia) si gana; 0 si pierde. */
private fun calcPayout(amount: Int, bet: RouletteBet, winningNumber: Int): Int {
    return when (bet) {
        is RouletteBet.ByColor -> {
            val colorWin = when (bet.color) {
                RouletteColor.ROJO -> winningNumber != 0 && getRouletteNumberColor(winningNumber) == Color(0xFFB71C1C)
                RouletteColor.NEGRO -> winningNumber != 0 && getRouletteNumberColor(winningNumber) == Color.Black
                RouletteColor.VERDE -> winningNumber == 0
            }
            if (!colorWin) 0 else if (bet.color == RouletteColor.VERDE) amount * 18 else amount * 2
        }
        is RouletteBet.ByNumber -> if (bet.number == winningNumber) amount * 36 else 0
    }
}
