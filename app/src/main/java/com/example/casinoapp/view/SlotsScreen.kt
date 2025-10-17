package com.example.casinoapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.model.CasinoUiState

@Composable
fun SlotsScreen(uiState: CasinoUiState, onPlay: (Int) -> Unit) {
    var bet by rememberSaveable { mutableStateOf("25") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Saldo: ${uiState.balance}")
        OutlinedTextField(
            value = bet,
            onValueChange = { bet = it.filter(Char::isDigit) },
            label = { Text("Apuesta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(onClick = { onPlay(bet.toIntOrNull() ?: 0) }, modifier = Modifier.fillMaxWidth()) {
            Text("Girar")
        }
        Text(
            "Premios: 3 iguales x4, 2 iguales x2; de lo contrario pierdes tu apuesta.",
            style = MaterialTheme.typography.bodySmall
        )

        // --- SECCIÓN VISUAL MEJORADA ---
        Spacer(Modifier.height(32.dp))

        if (uiState.slotResults.isNotEmpty()) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.slotResults.forEach { symbol ->
                        Text(
                            text = symbol.emoji,
                            fontSize = 64.sp // ¡Emojis grandes!
                        )
                    }
                }
            }
        } else {
            // Un espacio reservado para que la UI no "salte"
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)) {
                Text(
                    "¡Gira para jugar!",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}