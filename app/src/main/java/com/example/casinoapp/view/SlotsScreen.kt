package com.example.casinoapp.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SlotsScreen(balance: Int, onPlay: (Int) -> Unit) {
    var bet by rememberSaveable { mutableStateOf("25") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Saldo: $balance")
        OutlinedTextField(
            value = bet,
            onValueChange = { bet = it.filter(Char::isDigit) },
            label = { Text("Apuesta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(onClick = { onPlay(bet.toIntOrNull() ?: 0) }, modifier = Modifier.fillMaxWidth()) {
            Text("Girar")
        }
        Text("Premios: 3 iguales x4, 2 iguales x2; de lo contrario pierdes tu apuesta.", style = MaterialTheme.typography.bodySmall)
    }
}
