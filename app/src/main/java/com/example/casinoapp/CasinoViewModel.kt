package com.example.casinoapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.max
import kotlin.random.Random

data class CasinoUiState(
    val isLoggedIn: Boolean = false,
    val playerName: String = "",
    val balance: Int = 0,
    val statusMessage: String? = null,
    val history: List<String> = emptyList()
)

class CasinoViewModel : ViewModel() {

    var uiState by mutableStateOf(CasinoUiState())
        private set

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            uiState = uiState.copy(statusMessage = "Ingresa un usuario y contraseña válidos.")
            return
        }
        val cleanName = username.trim()
        uiState = CasinoUiState(
            isLoggedIn = true,
            playerName = cleanName,
            balance = 1000,
            statusMessage = "Bienvenido, $cleanName!"
        )
    }

    fun logout() {
        uiState = CasinoUiState(statusMessage = "Sesión cerrada.")
    }

    fun showMessage(message: String) {
        uiState = uiState.copy(statusMessage = message)
    }

    fun consumeMessage() {
        if (uiState.statusMessage != null) {
            uiState = uiState.copy(statusMessage = null)
        }
    }

    fun recordGameResult(balanceDelta: Int, description: String) {
        val newBalance = max(0, uiState.balance + balanceDelta)
        val updatedHistory = (listOf(description) + uiState.history).take(10)
        uiState = uiState.copy(
            balance = newBalance,
            statusMessage = description,
            history = updatedHistory
        )
    }

    fun canPlaceBet(amount: Int): Boolean {
        return amount in 1..uiState.balance
    }

    fun playRoulette(betAmount: Int, chosenColor: RouletteColor) {
        if (!canPlaceBet(betAmount)) {
            showMessage("Saldo insuficiente o apuesta inválida.")
            return
        }
        val winningColor = RouletteColor.values().random()
        val win = winningColor == chosenColor
        val delta = if (win) betAmount else -betAmount
        val message = if (win) {
            "Ganaste en la ruleta (${chosenColor.label}) +$betAmount"
        } else {
            "Perdiste en la ruleta (${winningColor.label}) -$betAmount"
        }
        recordGameResult(delta, message)
    }

    fun playBlackjack(betAmount: Int) {
        if (!canPlaceBet(betAmount)) {
            showMessage("Saldo insuficiente o apuesta inválida.")
            return
        }
        val playerHand = drawBlackjackHand()
        val dealerHand = drawBlackjackHand()
        val outcome = when {
            playerHand > 21 && dealerHand > 21 -> 0
            playerHand > 21 -> -betAmount
            dealerHand > 21 -> betAmount
            playerHand > dealerHand -> betAmount
            playerHand < dealerHand -> -betAmount
            else -> 0
        }
        val message = buildString {
            append("Blackjack: Jugador $playerHand vs Crupier $dealerHand ")
            when {
                outcome > 0 -> append("Ganaste +$betAmount")
                outcome < 0 -> append("Perdiste -$betAmount")
                else -> append("Empate (sin cambios)")
            }
        }
        recordGameResult(outcome, message)
    }

    private fun drawBlackjackHand(): Int {
        val cards = List(2) { drawCard() }
        var total = cards.sum()
        var aces = cards.count { it == 11 }
        while (total > 21 && aces > 0) {
            total -= 10
            aces -= 1
        }
        if (Random.nextBoolean()) {
            val card = drawCard()
            total += card
            if (card == 11 && total > 21) {
                total -= 10
            }
        }
        return total
    }

    private fun drawCard(): Int {
        val values = listOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 11)
        return values.random()
    }

    fun playSlots(betAmount: Int) {
        if (!canPlaceBet(betAmount)) {
            showMessage("Saldo insuficiente o apuesta inválida.")
            return
        }
        val symbols = SlotSymbol.values()
        val reels = List(3) { symbols.random() }
        val uniqueSymbols = reels.toSet()
        val outcome = when (uniqueSymbols.size) {
            1 -> betAmount * 4
            2 -> betAmount * 2
            else -> -betAmount
        }
        val reelLabels = reels.joinToString(" | ") { it.label }
        val message = if (outcome > 0) {
            "Tragamonedas: $reelLabels ganaste +$outcome"
        } else if (outcome < 0) {
            "Tragamonedas: $reelLabels perdiste -$betAmount"
        } else {
            "Tragamonedas: $reelLabels sin cambios"
        }
        recordGameResult(outcome, message)
    }
}

enum class RouletteColor(val label: String) {
    ROJO("Rojo"),
    NEGRO("Negro"),
    VERDE("Verde")
}

enum class SlotSymbol(val label: String) {
    TREBOL("Trébol"),
    SIETE("Siete"),
    DIAMANTE("Diamante"),
    CAMPANA("Campana"),
    CEREZA("Cereza")
}
