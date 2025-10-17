package com.example.casinoapp.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.model.CasinoUiState
import com.example.casinoapp.model.RouletteBet
import com.example.casinoapp.model.RouletteColor

private enum class BetMode { Color, Numero }

@Composable
fun RouletteScreen(
    uiState: CasinoUiState,
    onPlay: (Int, RouletteBet) -> Unit
) {
    var betAmount by rememberSaveable { mutableStateOf("50") }
    var betMode by rememberSaveable { mutableStateOf(BetMode.Color) }
    var selectedBet by remember { mutableStateOf<RouletteBet>(RouletteBet.ByColor(RouletteColor.ROJO)) }

    val rouletteState = uiState.rouletteState

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Saldo: ${uiState.balance}")
        OutlinedTextField(
            value = betAmount,
            onValueChange = { betAmount = it.filter(Char::isDigit) },
            label = { Text("Apuesta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        TabRow(selectedTabIndex = betMode.ordinal) {
            BetMode.values().forEach { mode ->
                Tab(
                    selected = betMode == mode,
                    onClick = {
                        betMode = mode
                        selectedBet = if (mode == BetMode.Color) {
                            RouletteBet.ByColor(RouletteColor.ROJO)
                        } else {
                            RouletteBet.ByNumber(1)
                        }
                    },
                    text = { Text(mode.name) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when (betMode) {
            BetMode.Color -> ColorBetSelector(currentBet = selectedBet, onBetSelected = { selectedBet = it })
            BetMode.Numero -> NumberBetSelector(currentBet = selectedBet, onBetSelected = { selectedBet = it })
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { onPlay(betAmount.toIntOrNull() ?: 0, selectedBet) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Jugar ruleta")
        }
        Text("Pagos: Color 1:1, Verde 17:1, Número 35:1", style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(16.dp))

        WinningNumberDisplay(rouletteState.winningNumber)
    }
}

@Composable
private fun ColorBetSelector(currentBet: RouletteBet, onBetSelected: (RouletteBet.ByColor) -> Unit) {
    val selectedColor = (currentBet as? RouletteBet.ByColor)?.color
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        RouletteColor.values().forEach { color ->
            val border = if (selectedColor == color) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else
            OutlinedButton(
                onClick = { onBetSelected(RouletteBet.ByColor(color)) },
                modifier = Modifier.weight(1f),
            ) { Text(color.label) }
        }
    }
}

@Composable
private fun NumberBetSelector(currentBet: RouletteBet, onBetSelected: (RouletteBet.ByNumber) -> Unit) {
    val selectedNumber = (currentBet as? RouletteBet.ByNumber)?.number
    val numbers = (0..36).toList()

    Text("Selecciona un número", style = MaterialTheme.typography.titleMedium)

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                Text(text = number.toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WinningNumberDisplay(winningNumber: Int?) {
    if (winningNumber != null) {
        val resultColor = getRouletteNumberColor(winningNumber)
        Text("Número Ganador", style = MaterialTheme.typography.titleMedium)
        Box(
            modifier = Modifier.size(120.dp).clip(CircleShape).background(resultColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$winningNumber", fontSize = 50.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    } else {
        Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
            Text("¡Haz tu apuesta!", modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.headlineSmall)
        }
    }
}

private fun getRouletteNumberColor(number: Int): Color {
    return when {
        number == 0 -> Color(0xFF008000) // Verde
        number in setOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36) -> Color(0xFFB71C1C)
        else -> Color.Black
    }
}