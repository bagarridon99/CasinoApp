package com.example.casinoapp.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.model.BlackjackGameState

@Composable
fun BlackjackScreen(
    uiState: BlackjackGameState,
    balance: Int,
    onStartGame: (Int) -> Unit,
    onHit: () -> Unit,
    onStand: () -> Unit
) {
    var bet by rememberSaveable { mutableStateOf("100") }
    val isGameInProgress = uiState.isPlayerTurn

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Saldo: $balance")

        // Controles de apuesta y botón de Jugar/Repartir
        AnimatedVisibility(!isGameInProgress) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = bet,
                    onValueChange = { bet = it.filter(Char::isDigit) },
                    label = { Text("Apuesta") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onStartGame(bet.toIntOrNull() ?: 0) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Repartir Cartas")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Mesa de juego
        if (uiState.playerHand.isNotEmpty()) {
            // Mano del crupier
            val dealerTotal = if(isGameInProgress) cardValueToString(uiState.dealerHand.first()) + " + ?" else handTotal(uiState.dealerHand).toString()
            Text("Mano del Crupier (Total: $dealerTotal)", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.height(70.dp)) {
                // Muestra la primera carta del crupier. La segunda está oculta si es el turno del jugador.
                CardView(cardValueToString(uiState.dealerHand.first()))
                if (isGameInProgress) {
                    HiddenCardView()
                } else {
                    uiState.dealerHand.drop(1).forEach { cardValue ->
                        CardView(cardValueToString(cardValue))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Mano del jugador
            val playerTotal = handTotal(uiState.playerHand).toString()
            Text("Tu Mano (Total: $playerTotal)", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.height(70.dp)) {
                uiState.playerHand.forEach { cardValue ->
                    CardView(cardValueToString(cardValue))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Mensaje del juego
            uiState.gameMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Botones de acción del jugador
        AnimatedVisibility(isGameInProgress) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onHit, modifier = Modifier.weight(1f)) {
                    Text("Pedir Carta")
                }
                Button(onClick = onStand, modifier = Modifier.weight(1f)) {
                    Text("Plantarse")
                }
            }
        }
    }
}

@Composable
private fun HiddenCardView() {
    Card(
        modifier = Modifier.size(width = 50.dp, height = 70.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}

@Composable
private fun CardView(value: String) {
    Card(
        modifier = Modifier.size(width = 50.dp, height = 70.dp),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun cardValueToString(value: Int): String {
    return when (value) {
        11 -> "A"
        10 -> "K"
        else -> value.toString()
    }
}

private fun handTotal(cards: List<Int>): Int {
    var total = cards.sum()
    var aces = cards.count { it == 11 }
    while (total > 21 && aces > 0) {
        total -= 10
        aces--
    }
    return total
}