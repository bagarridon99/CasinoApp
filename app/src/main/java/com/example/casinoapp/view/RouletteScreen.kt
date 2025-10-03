package com.example.casinoapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.casinoapp.model.RouletteColor

@Composable
fun RouletteScreen(balance: Int, onPlay: (Int, RouletteColor) -> Unit) {
    var bet by rememberSaveable { mutableStateOf("50") }
    var color by rememberSaveable { mutableStateOf(RouletteColor.ROJO) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Saldo: $balance")
        OutlinedTextField(
            value = bet,
            onValueChange = { bet = it.filter(Char::isDigit) },
            label = { Text("Apuesta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RouletteColor.values().forEach { c ->
                OutlinedButton(
                    onClick = { color = c },
                    modifier = Modifier.weight(1f)
                ) { Text(c.label + if (c == color) " ✓" else "") }
            }
        }
        Button(onClick = { onPlay(bet.toIntOrNull() ?: 0, color) }, modifier = Modifier.fillMaxWidth()) {
            Text("Jugar ruleta")
        }
        Text("Pagos: Rojo/Negro 1:1, Verde 17:1", style = MaterialTheme.typography.bodySmall)
    }
}
