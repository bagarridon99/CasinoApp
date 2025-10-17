
package com.example.casinoapp.model

data class GameResult(
    val description: String,
    val delta: Int,
    val winningNumber: Int? = null,
    val playerHand: List<Int> = emptyList(),
    val dealerHand: List<Int> = emptyList(),
    val slotResults: List<SlotSymbol> = emptyList()
)