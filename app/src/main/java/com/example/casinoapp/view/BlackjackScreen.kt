@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.casinoapp.view

import android.app.Application
import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.R
import com.example.casinoapp.model.BlackjackGameState
import com.example.casinoapp.ui.common.GameHeader
import com.example.casinoapp.ui.common.formatCLP
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Colores VIP Blackjack ---
private val GoldAccent = Color(0xFFFFD700)
private val GoldDark = Color(0xFFC5A000)
private val FeltGreen = Color(0xFF0F3D0F)
private val FeltGreenDark = Color(0xFF051C05)
private val TableGradient = Brush.radialGradient(colors = listOf(FeltGreen, FeltGreenDark), radius = 1200f)
private val TieColor = Color(0xFF424242) // Gris para el Empate

@Composable
fun BlackjackScreen(
    uiState: BlackjackGameState,
    balance: Int,
    onStartGame: (Int) -> Unit,
    onHit: () -> Unit,
    onStand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var bet by rememberSaveable { mutableStateOf("100") }
    val betInt = bet.toIntOrNull() ?: 0
    val canStart = betInt in 1..balance
    val isGameInProgress = uiState.isPlayerTurn

    var outcomeText by remember { mutableStateOf<String?>(null) }

    // --- ESTADO DE RESULTADO: 0 = Pierde, 1 = Gana, 2 = Empate ---
    var outcomeStatus by remember { mutableStateOf(0) }
    var showOutcomeBanner by remember { mutableStateOf(false) }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = GoldAccent,
        unfocusedBorderColor = Color.Gray,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color.Black.copy(0.8f),
        unfocusedContainerColor = Color.Black.copy(0.6f),
        focusedLabelColor = GoldAccent,
        unfocusedLabelColor = Color.White
    )

    // Lógica de Reacción a Resultados incluyendo Empate
    LaunchedEffect(uiState.gameMessage) {
        uiState.gameMessage?.let { msg ->
            outcomeText = msg

            // Detectar el tipo de resultado por el texto del mensaje
            outcomeStatus = when {
                msg.contains("Ganaste", true) || msg.contains("Blackjack", true) -> 1
                msg.contains("Empate", true) || msg.contains("Push", true) -> 2
                else -> 0
            }

            showOutcomeBanner = true
            delay(1000)
            showOutcomeBanner = false
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item { GameHeader(imageRes = R.drawable.blackjack_background, balance = balance) }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(TableGradient)
                        .border(6.dp, Color(0xFF3E2723), RoundedCornerShape(32.dp))
                ) {
                    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        if (uiState.playerHand.isNotEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Text("CRUPIER", color = GoldAccent, fontWeight = FontWeight.Bold)
                                CardsRow(cards = uiState.dealerHand, dealer = true, hiddenSecond = isGameInProgress)
                                if (!isGameInProgress) TotalBadge(handTotal(uiState.dealerHand))
                            }
                        }

                        if (uiState.playerHand.isNotEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                TotalBadge(handTotal(uiState.playerHand))
                                CardsRow(cards = uiState.playerHand)
                                Text("TÚ", color = GoldAccent, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Haz tu apuesta para jugar", color = Color.White.copy(0.6f))
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(0.4f))
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (isGameInProgress) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(onClick = onHit, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))) { Text("PEDIR") }
                                Button(onClick = onStand, modifier = Modifier.weight(1f).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))) { Text("PLANTAR") }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = bet,
                                    onValueChange = { bet = it.filter(Char::isDigit) },
                                    label = { Text("Apuesta") },
                                    colors = tfColors,
                                    textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Button(
                                    onClick = { onStartGame(betInt) },
                                    enabled = canStart,
                                    modifier = Modifier.height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark)
                                ) { Text("JUGAR", color = Color.Black, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }
        }

        // --- ANUNCIO GANAR/PERDER/EMPATE: TOTALMENTE CENTRADO ---
        AnimatedVisibility(
            visible = showOutcomeBanner && outcomeText != null,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when(outcomeStatus) {
                        1 -> Color(0xFF1B5E20) // Verde si gana
                        2 -> TieColor          // Gris si empata
                        else -> Color(0xFFB71C1C) // Rojo si pierde
                    }
                ),
                elevation = CardDefaults.cardElevation(16.dp),
                border = BorderStroke(2.dp, GoldAccent)
            ) {
                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = outcomeText?.uppercase() ?: "",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    when(outcomeStatus) {
                        1 -> {
                            Spacer(Modifier.height(8.dp))
                            Text("¡FELICIDADES!", color = GoldAccent, fontWeight = FontWeight.Bold)
                        }
                        2 -> {
                            Spacer(Modifier.height(8.dp))
                            Text("PUNTO PARA AMBOS", color = Color.LightGray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalBadge(total: Int) {
    Surface(color = Color.Black.copy(0.6f), shape = RoundedCornerShape(50), border = BorderStroke(1.dp, GoldAccent)) {
        Text(text = "$total", color = GoldAccent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 14.sp)
    }
}

@Composable
private fun CardsRow(cards: List<Int>, dealer: Boolean = false, hiddenSecond: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy((-30).dp), modifier = Modifier.height(110.dp)) {
        cards.forEachIndexed { index, card ->
            if (dealer && index == 1 && hiddenSecond) {
                Box(Modifier.size(70.dp, 100.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFB71C1C)).border(2.dp, Color.White, RoundedCornerShape(6.dp)))
            } else {
                Surface(Modifier.size(70.dp, 100.dp).rotate(Random.nextInt(-3, 3).toFloat()), shape = RoundedCornerShape(6.dp), color = Color.White) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = if(card==11) "A" else card.toString(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

private fun handTotal(cards: List<Int>): Int {
    var total = cards.sum()
    var aces = cards.count { it == 11 }
    while (total > 21 && aces > 0) { total -= 10; aces-- }
    return total
}