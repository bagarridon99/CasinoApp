package com.example.casinoapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.casinoapp.model.*
import com.example.casinoapp.repository.CasinoRepository
import com.example.casinoapp.repository.InMemoryCasinoRepository
import kotlin.math.max

private const val START_BALANCE = 1000
private const val HISTORY_LIMIT = 10

class CasinoViewModel(
    private val repo: CasinoRepository = InMemoryCasinoRepository()
) : ViewModel() {

    var uiState by mutableStateOf(CasinoUiState())
        private set

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            showMessage("Ingresa un usuario y contraseña válidos.")
            return
        }
        val name = username.trim()
        uiState = CasinoUiState(
            isLoggedIn = true,
            playerName = name,
            balance = START_BALANCE,
            statusMessage = "¡Bienvenido, $name!"
        )
    }

    fun logout() {
        uiState = CasinoUiState(statusMessage = "Sesión cerrada.")
    }

    fun consumeMessage() {
        uiState = uiState.copy(statusMessage = null)
    }

    private fun showMessage(msg: String) {
        uiState = uiState.copy(statusMessage = msg)
    }

    private fun push(delta: Int, description: String) {
        val newBalance = max(0, uiState.balance + delta)
        val newHistory = (listOf(description) + uiState.history).take(HISTORY_LIMIT)
        uiState = uiState.copy(
            balance = newBalance,
            statusMessage = description,
            history = newHistory
        )
    }

    private fun canBet(amount: Int): Boolean = amount in 1..uiState.balance

    fun deposit(amount: Int) {
        if (amount <= 0) {
            showMessage("Monto de depósito inválido.")
            return
        }
        push(amount, "Depósito realizado +$amount")
    }

    fun withdraw(amount: Int) {
        if (!canBet(amount)) {
            showMessage("Monto de retiro inválido o saldo insuficiente.")
            return
        }
        push(-amount, "Retiro realizado -$amount")
    }

    fun playRoulette(betAmount: Int, bet: RouletteBet) {
        if (!canBet(betAmount)) {
            showMessage("Saldo insuficiente o apuesta inválida.")
            return
        }
        val result: GameResult = repo.playRoulette(betAmount, bet)
        push(result.delta, result.description)
        uiState = uiState.copy(
            rouletteState = RouletteGameState(winningNumber = result.winningNumber)
        )
    }

    fun playSlots(bet: Int) {
        if (!canBet(bet)) {
            showMessage("Saldo insuficiente o apuesta inválida.")
            return
        }
        val result: GameResult = repo.playSlots(bet)
        push(result.delta, result.description)
        uiState = uiState.copy(slotResults = result.slotResults)
    }

    private var blackjackBet = 0

    fun startBlackjack(bet: Int) {
        if (!canBet(bet)) {
            showMessage("Saldo insuficiente o apuesta inválida.")
            return
        }
        blackjackBet = bet
        val playerHand = listOf(repo.drawCard(), repo.drawCard())
        val dealerHand = listOf(repo.drawCard(), repo.drawCard())

        if (handTotal(playerHand) == 21) {
            endBlackjackTurn(playerHand, dealerHand)
        } else {
            uiState = uiState.copy(
                blackjackState = BlackjackGameState(
                    playerHand = playerHand,
                    dealerHand = dealerHand,
                    isPlayerTurn = true,
                    gameMessage = "Tu turno. ¿Pides o te plantas?"
                )
            )
        }
    }

    fun blackjackHit() {
        if (!uiState.blackjackState.isPlayerTurn) return
        val newHand = uiState.blackjackState.playerHand + repo.drawCard()
        if (handTotal(newHand) > 21) {
            endBlackjackTurn(newHand, uiState.blackjackState.dealerHand, "¡Te has pasado! Gana la casa.")
        } else {
            uiState = uiState.copy(
                blackjackState = uiState.blackjackState.copy(playerHand = newHand)
            )
        }
    }

    fun blackjackStand() {
        if (!uiState.blackjackState.isPlayerTurn) return
        endBlackjackTurn(uiState.blackjackState.playerHand, uiState.blackjackState.dealerHand)
    }

    private fun endBlackjackTurn(playerHand: List<Int>, dealerHand: List<Int>, customMessage: String? = null) {
        var currentDealerHand = dealerHand
        while (handTotal(currentDealerHand) < 17) {
            currentDealerHand = currentDealerHand + repo.drawCard()
        }

        val playerTotal = handTotal(playerHand)
        val dealerTotal = handTotal(currentDealerHand)

        val resultMessage: String
        val delta: Int

        if (customMessage != null) {
            resultMessage = customMessage
            delta = -blackjackBet
        } else if (playerTotal > 21) {
            resultMessage = "Te has pasado de 21. Gana la casa."
            delta = -blackjackBet
        } else if (dealerTotal > 21) {
            resultMessage = "¡El crupier se ha pasado! ¡Ganas!"
            delta = blackjackBet
        } else if (playerTotal > dealerTotal) {
            resultMessage = "¡Tu mano es mayor! ¡Ganas!"
            delta = blackjackBet
        } else if (dealerTotal > playerTotal) {
            resultMessage = "La mano del crupier es mayor. Gana la casa."
            delta = -blackjackBet
        } else {
            resultMessage = "¡Empate!"
            delta = 0
        }

        push(delta, "Blackjack: $resultMessage")
        uiState = uiState.copy(
            blackjackState = BlackjackGameState(
                playerHand = playerHand,
                dealerHand = currentDealerHand,
                isPlayerTurn = false,
                gameMessage = resultMessage + " Juega de nuevo."
            )
        )
    }

    private fun handTotal(cards: List<Int>): Int {
        var total = cards.sum()
        var aces = cards.count { it == 11 }
        while (total > 21 && aces > 0) {
            total -= 10
            aces--
        }
        return total
    }
}