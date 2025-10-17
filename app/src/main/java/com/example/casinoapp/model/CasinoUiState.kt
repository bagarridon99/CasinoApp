package com.example.casinoapp.model
data class CasinoUiState(
    val isLoggedIn: Boolean = false,
    val playerName: String = "",
    val balance: Int = 0,
    val statusMessage: String? = null,
    val history: List<String> = emptyList(),
    // Propiedades para los juegos
    val rouletteState: RouletteGameState = RouletteGameState(),
    val slotResults: List<SlotSymbol> = emptyList(),
    val blackjackState: BlackjackGameState = BlackjackGameState()
)
// clase para el blackjack
data class BlackjackGameState(
    val playerHand: List<Int> = emptyList(),
    val dealerHand: List<Int> = emptyList(),
    val isPlayerTurn: Boolean = false,
    val gameMessage: String? = null
)

// clase para la ruleta
data class RouletteGameState(
    val winningNumber: Int? = null
)