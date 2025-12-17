@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.casinoapp.view

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.casinoapp.R
import com.example.casinoapp.model.CasinoUiState
import com.example.casinoapp.model.RouletteBet
import com.example.casinoapp.model.RouletteColor
import com.example.casinoapp.ui.common.GameHeader
import com.example.casinoapp.ui.common.formatCLP
import com.example.casinoapp.ui.common.RouletteWheel
import kotlinx.coroutines.delay

// --- COLORES PREMIUM RULETA ---
private val GoldLight = Color(0xFFFFD700)
private val GoldDark = Color(0xFFC5A000)
private val GoldBrush = Brush.linearGradient(colors = listOf(GoldDark, GoldLight, GoldDark))
private val WoodDark = Color(0xFF21110E)
private val WoodLight = Color(0xFF5D4037)
private val WoodBrush = Brush.radialGradient(colors = listOf(WoodLight, WoodDark, Color.Black))
private val FeltGreen = Color(0xFF0F3D0F)

private enum class BetMode { Color, Numero }
private data class Outcome(val positive: Boolean, val wonAmount: Int, val lostAmount: Int)

@Composable
fun RouletteScreen(
    uiState: CasinoUiState,
    onPlay: (Int, RouletteBet) -> Unit,
    modifier: Modifier = Modifier
) {
    var betAmount by rememberSaveable { mutableStateOf("100") }
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

    // Lógica de resultado
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

    LaunchedEffect(showOutcome) {
        if (showOutcome) {
            delay(4000) // Duración del anuncio
            showOutcome = false
        }
    }

    // BOX para permitir la superposición (Overlay) del mensaje centrado
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Capa 1: Contenido del Juego (Scrollable)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { GameHeader(imageRes = R.drawable.roulette_background, balance = uiState.balance) }

            item {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(340.dp)) {
                    Box(Modifier.fillMaxSize().shadow(20.dp, CircleShape).background(WoodBrush, CircleShape).border(8.dp, Color(0xFF150B09), CircleShape))
                    Box(Modifier.fillMaxSize(0.92f).background(Color.Transparent, CircleShape).border(6.dp, GoldBrush, CircleShape))
                    Box(Modifier.fillMaxSize(0.88f).background(Color.Black, CircleShape))
                    RouletteWheel(modifier = Modifier.fillMaxSize(0.85f), size = 280.dp, targetNumber = winningNumber, spinTrigger = spinCount)
                    Canvas(modifier = Modifier.align(Alignment.TopCenter).size(30.dp).offset(y = 12.dp)) {
                        val path = Path().apply {
                            moveTo(size.width / 2f, size.height)
                            lineTo(size.width, 0f)
                            lineTo(0f, 0f)
                            close()
                        }
                        drawPath(path, brush = GoldBrush)
                    }
                    Box(Modifier.size(24.dp).background(GoldBrush, CircleShape).border(1.dp, Color.Black.copy(0.5f), CircleShape).shadow(4.dp, CircleShape))
                }
            }

            item {
                AnimatedVisibility(visible = winningNumber != null) {
                    WinningNumberLabel(winningNumber)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = FeltGreen)
                ) {
                    Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("HAGA SU APUESTA", style = MaterialTheme.typography.labelLarge, color = GoldLight.copy(alpha = 0.7f), modifier = Modifier.align(Alignment.CenterHorizontally))

                        TabRow(
                            selectedTabIndex = betMode.ordinal,
                            containerColor = Color.Transparent,
                            contentColor = GoldLight,
                            divider = {},
                            indicator = { tabPositions -> TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(tabPositions[betMode.ordinal]), color = GoldLight, height = 3.dp) }
                        ) {
                            BetMode.values().forEach { mode ->
                                Tab(selected = betMode == mode, onClick = { betMode = mode }, text = { Text(if (mode == BetMode.Color) "COLOR" else "NÚMERO", fontWeight = FontWeight.Bold) })
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = betAmount,
                                onValueChange = { betAmount = it.filter(Char::isDigit) },
                                label = { Text("Monto", color = Color.LightGray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldLight, unfocusedBorderColor = Color.Gray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ChipButton("100") { betAmount = "100" }
                            Spacer(Modifier.width(4.dp))
                            ChipButton("MAX") { betAmount = uiState.balance.toString() }
                        }

                        Box(modifier = Modifier.background(Color(0xFF0A290A), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(8.dp)).padding(8.dp)) {
                            when (betMode) {
                                BetMode.Color -> ColorBetSelector(selectedBet) { selectedBet = it }
                                BetMode.Numero -> NumberBetSelector(selectedBet) { selectedBet = it }
                            }
                        }

                        Button(
                            onClick = { onPlay(amountInt, selectedBet); spinCount += 1 },
                            enabled = canBet,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldDark, disabledContainerColor = Color.DarkGray),
                            elevation = ButtonDefaults.buttonElevation(6.dp)
                        ) {
                            Text("GIRAR", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        PayoutLegendBlock()
                    }
                }
            }
        }

        // Capa 2: ANUNCIO CENTRADO (Gane/Perdí)
        AnimatedVisibility(
            visible = showOutcome && lastOutcome != null,
            enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.8f),
            exit = fadeOut(tween(500)) + scaleOut(targetScale = 0.8f),
            modifier = Modifier.align(Alignment.Center) // <--- CENTRADO TOTAL
        ) {
            Card(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    // VERDE SI GANA, ROJO SI PIERDE
                    containerColor = if (lastOutcome?.positive == true) Color(0xFF1B5E20) else Color(0xFFB71C1C)
                ),
                border = BorderStroke(2.dp, GoldLight)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (lastOutcome?.positive == true) "¡GANASTE!" else "PERDISTE",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (lastOutcome?.positive == true)
                            "+ ${formatCLP(lastOutcome!!.wonAmount)}"
                        else
                            "- ${formatCLP(lastOutcome!!.lostAmount)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight
                    )
                }
            }
        }
    }
}

// --- SUB-COMPONENTES (Sin cambios necesarios aquí) ---

@Composable
private fun ChipButton(text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = CircleShape, color = GoldDark, border = BorderStroke(1.dp, Color.White.copy(0.5f)), modifier = Modifier.size(48.dp), shadowElevation = 4.dp) {
        Box(contentAlignment = Alignment.Center) { Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black) }
    }
}

@Composable
private fun ColorBetSelector(current: RouletteBet, onSelect: (RouletteBet.ByColor) -> Unit) {
    val selected = (current as? RouletteBet.ByColor)?.color
    Row(Modifier.fillMaxWidth().height(80.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(RouletteColor.ROJO, RouletteColor.NEGRO).forEach { color ->
            val isSelected = selected == color
            val bg = if (color == RouletteColor.ROJO) Color(0xFFB71C1C) else Color.Black
            Box(modifier = Modifier.weight(1f).fillMaxHeight().shadow(if(isSelected) 8.dp else 2.dp, RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)).background(bg).border(if (isSelected) 3.dp else 0.dp, GoldLight, RoundedCornerShape(8.dp)).clickable { onSelect(RouletteBet.ByColor(color)) }, contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(20.dp)) { rotate(45f) { drawRect(color = Color.White.copy(0.2f)) } }
                Text(color.label.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun NumberBetSelector(current: RouletteBet, onSelect: (RouletteBet.ByNumber) -> Unit) {
    val selectedNumber = (current as? RouletteBet.ByNumber)?.number
    LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 44.dp), modifier = Modifier.height(220.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        items((0..36).toList()) { number ->
            val color = getRouletteNumberColor(number)
            val isSelected = selectedNumber == number
            Box(modifier = Modifier.aspectRatio(1f).shadow(2.dp, RoundedCornerShape(4.dp)).clip(RoundedCornerShape(4.dp)).background(if (isSelected) GoldLight else color).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(4.dp)).clickable { onSelect(RouletteBet.ByNumber(number)) }, contentAlignment = Alignment.Center) {
                Text("$number", color = if (isSelected) Color.Black else Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun WinningNumberLabel(number: Int?) {
    if (number != null) {
        val color = getRouletteNumberColor(number)
        Surface(color = Color.Black.copy(0.8f), shape = RoundedCornerShape(50), border = BorderStroke(1.dp, GoldDark), modifier = Modifier.padding(vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text("RESULTADO: ", color = GoldLight, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.size(32.dp).background(color, CircleShape).border(1.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                    Text("$number", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PayoutLegendBlock() {
    Row(Modifier.fillMaxWidth().padding(top=8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Color pago 1:1", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.5f))
        Text("Número pago 35:1", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.5f))
    }
}

private fun getRouletteNumberColor(number: Int): Color = when {
    number == 0 -> Color(0xFF006400)
    number in setOf(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36) -> Color(0xFFB71C1C)
    else -> Color.Black
}

private fun calcPayout(amount: Int, bet: RouletteBet, winningNumber: Int): Int {
    return when (bet) {
        is RouletteBet.ByColor -> {
            val colorWin = when (bet.color) {
                RouletteColor.ROJO -> winningNumber != 0 && getRouletteNumberColor(winningNumber) == Color(0xFFB71C1C)
                RouletteColor.NEGRO -> winningNumber != 0 && getRouletteNumberColor(winningNumber) == Color.Black
                RouletteColor.VERDE -> winningNumber == 0
            }
            if (colorWin) (if (bet.color == RouletteColor.VERDE) amount * 18 else amount * 2) else 0
        }
        is RouletteBet.ByNumber -> if (bet.number == winningNumber) amount * 36 else 0
    }
}