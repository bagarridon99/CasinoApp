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
fun BlackjackScreen(balance: Int, onPlay: (Int) -> Unit) {
    var bet by rememberSaveable { mutableStateOf("100") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Saldo: $balance")
        OutlinedTextField(
            value = bet,
            onValueChange = { bet = it.filter(Char::isDigit) },
            label = { Text("Apuesta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(onClick = { onPlay(bet.toIntOrNull() ?: 0) }, modifier = Modifier.fillMaxWidth()) {
            Text("Pedir cartas")
        }
        Text("El crupier pide hasta 17; si se pasa de 21 pierde.", style = MaterialTheme.typography.bodySmall)
    }
}
