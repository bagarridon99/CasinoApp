package com.example.casinoapp.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
data class GameResult(
    val description: String,
    val delta: Int,
    val winningNumber: Int? = null,
    val playerHand: List<Int> = emptyList(),
    val dealerHand: List<Int> = emptyList(),
    val slotResults: List<SlotSymbol> = emptyList(),
    val timestamp: String = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault()).format(Date())
)
