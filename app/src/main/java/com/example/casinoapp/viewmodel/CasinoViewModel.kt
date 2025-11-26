package com.example.casinoapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.casinoapp.data.AppDatabase
import com.example.casinoapp.data.SessionManager
import com.example.casinoapp.model.*
import com.example.casinoapp.repository.CasinoRepository
import com.example.casinoapp.repository.InMemoryCasinoRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.max

class CasinoViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: CasinoRepository = InMemoryCasinoRepository()
    private val userDao = AppDatabase.get(app).userDao()
    private val prefs = app.getSharedPreferences("casino_prefs", Context.MODE_PRIVATE)
    private var currentUserId: Long = 0L

    var uiState by mutableStateOf(CasinoUiState())
        private set

    init { loadUserData() }

    fun loadUserData() = viewModelScope.launch {
        // 🔁 Antes: SessionManager.emailFlow(...)
        val email = SessionManager.getEmail(getApplication()).firstOrNull()

        if (email.isNullOrBlank()) {
            uiState = CasinoUiState(isLoggedIn = false)
            return@launch
        }

        val user = userDao.getByEmail(email)
        if (user == null) {
            uiState = CasinoUiState(isLoggedIn = false)
            return@launch
        }

        currentUserId = user.id
        uiState = CasinoUiState(
            isLoggedIn = true,
            profile = UserProfile(
                nombre = user.username,
                nivel = 1,
                xpActual = 0
            ),
            balance = user.balance,
            statusMessage = "¡Bienvenido, ${user.username}!"
        )
    }


    fun logout() = viewModelScope.launch {
        SessionManager.clear(getApplication())
        uiState = CasinoUiState(statusMessage = "Sesión cerrada.", isLoggedIn = false)
    }

    fun consumeMessage() { uiState = uiState.copy(statusMessage = null) }

    private fun push(delta: Int, description: String) {
        val newBalance = max(0, uiState.balance + delta)
        val newHistory = (listOf(description) + uiState.history).take(10)
        uiState = uiState.copy(balance = newBalance, statusMessage = description, history = newHistory)
        if (currentUserId != 0L) viewModelScope.launch { userDao.updateBalance(currentUserId, newBalance) }
    }

    fun deposit(amount: Int) { if (amount > 0) push(amount, "Depósito +$amount") }
    fun withdraw(amount: Int) { if (amount in 1..uiState.balance) push(-amount, "Retiro -$amount") }

    fun claimDailyBonus() {
        val last = prefs.getLong("last_bonus", 0L)
        if (System.currentTimeMillis() - last < 86400000) return
        push(100, "Bono Diario +100")
        prefs.edit().putLong("last_bonus", System.currentTimeMillis()).apply()
    }

    fun playRoulette(betAmount: Int, bet: RouletteBet) {
        if (betAmount !in 1..uiState.balance) return
        val result = repo.playRoulette(betAmount, bet)
        push(result.delta, result.description)
        uiState = uiState.copy(rouletteState = RouletteGameState(result.winningNumber))
    }

    fun playSlots(bet: Int) {
        if (bet !in 1..uiState.balance) return
        val result = repo.playSlots(bet)
        push(result.delta, result.description)
        uiState = uiState.copy(slotResults = result.slotResults)
    }

    private var bjBet = 0
    fun startBlackjack(bet: Int) {
        if (bet !in 1..uiState.balance) return
        bjBet = bet
        val pH = listOf(repo.drawCard(), repo.drawCard())
        val dH = listOf(repo.drawCard(), repo.drawCard())
        if (handTotal(pH) == 21) endBj(pH, dH) else uiState = uiState.copy(blackjackState = BlackjackGameState(pH, dH, true, "Tu turno"))
    }
    fun blackjackHit() {
        if (!uiState.blackjackState.isPlayerTurn) return
        val newH = uiState.blackjackState.playerHand + repo.drawCard()
        if (handTotal(newH) > 21) endBj(newH, uiState.blackjackState.dealerHand) else uiState = uiState.copy(blackjackState = uiState.blackjackState.copy(playerHand = newH))
    }
    fun blackjackStand() { endBj(uiState.blackjackState.playerHand, uiState.blackjackState.dealerHand) }
    private fun endBj(p: List<Int>, d: List<Int>) {
        var dH = d
        if (handTotal(p) <= 21) while (handTotal(dH) < 17) dH = dH + repo.drawCard()
        val pT = handTotal(p); val dT = handTotal(dH)
        val delta = when { pT > 21 -> -bjBet; dT > 21 -> bjBet; pT > dT -> bjBet; dT > pT -> -bjBet; else -> 0 }
        push(delta, if(delta>0) "Ganaste BJ" else "Perdiste BJ")
        uiState = uiState.copy(blackjackState = BlackjackGameState(p, dH, false, if(delta>0) "Ganaste" else "Perdiste"))
    }
    private fun handTotal(c: List<Int>): Int { var s = c.sum(); var a = c.count{it==11}; while(s>21 && a>0){s-=10;a--}; return s }
}