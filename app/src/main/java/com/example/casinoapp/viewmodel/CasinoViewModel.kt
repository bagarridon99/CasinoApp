package com.example.casinoapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.casinoapp.model.CasinoUiState
import com.example.casinoapp.model.GameResult
import com.example.casinoapp.model.RouletteColor
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

    /* ---------------- Auth ---------------- */

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

    /* --------------- UI messages --------------- */

    private fun showMessage(msg: String) {
        uiState = uiState.copy(statusMessage = msg)
    }

    fun consumeMessage() {
        if (uiState.statusMessage != null) {
            uiState = uiState.copy(statusMessage = null)
        }
    }

    /* --------------- Helpers --------------- */

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

    /* --------------- Wallet --------------- */

    fun deposit(amount: Int) {
        if (amount <= 0) {
            showMessage("Monto de depósito inválido.")
            return
        }
        push(amount, "Depósito realizado +$amount")
    }

    fun withdraw(amount: Int) {
        if (amount <= 0) {
            showMessage("Monto de retiro inválido.")
            return
        }
        if (amount > uiState.balance) {
            showMessage("Saldo insuficiente para retirar $amount.")
            return
        }
        push(-amount, "Retiro realizado -$amount")
    }

    /* --------------- Games (delegan al repo) --------------- */

    fun playRoulette(bet: Int, color: RouletteColor) {
        if (!canBet(bet)) {
            showMessage("Saldo insuficiente o apuesta inválida.")
            return
        }
        val result: GameResult = repo.playRoulette(bet, color)
        push(result.delta, result.description)
    }

    fun playBlackjack(bet: Int) {
        if (!canBet(bet)) {
            showMessage("Saldo insuficiente o apuesta inválida.")
            return
        }
        val result: GameResult = repo.playBlackjack(bet)
        push(result.delta, result.description)
    }

    fun playSlots(bet: Int) {
        if (!canBet(bet)) {
            showMessage("Saldo insuficiente o apuesta inválida.")
            return
        }
        val result: GameResult = repo.playSlots(bet)
        push(result.delta, result.description)
    }
}
