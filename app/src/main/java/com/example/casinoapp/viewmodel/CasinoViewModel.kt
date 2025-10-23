package com.example.casinoapp.viewmodel

import android.app.Application
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
import kotlinx.coroutines.flow.firstOrNull // <--- AÑADIDO
import kotlinx.coroutines.launch
import kotlin.math.max

// Constantes de dominio: saldo inicial, límite de historial y bono diario.
private const val START_BALANCE = 1000
private const val HISTORY_LIMIT = 10
private const val BONUS_AMOUNT = 100 // Constante para el bono

/**
 * ViewModel principal del “casino”.
 * - Carga datos del usuario desde Room usando el email de SessionManager.
 * - Expone un CasinoUiState observable (mutableStateOf) para Compose.
 * - Llama a un CasinoRepository (aquí, implementación en memoria) para la lógica de juegos.
 * - Persiste cambios de balance en Room.
 */
class CasinoViewModel(
    app: Application
) : AndroidViewModel(app) {

    // Implementación concreta del repositorio (in-memory, sin backend)
    private val repo: CasinoRepository = InMemoryCasinoRepository()
    private val userDao = AppDatabase.get(app).userDao()

    private var currentUserId: Long = 0L // ID del usuario actual para sincronizar balance
    var uiState by mutableStateOf(CasinoUiState())
        private set

    init {
        loadUserData()
    }

    /**
     * Carga el usuario activo según SessionManager.emailFlow:
     * - Si no hay sesión, resetea estado (isLoggedIn=false).
     * - Si hay email, consulta Room por el usuario y pobla CasinoUiState.
     */
    fun loadUserData() = viewModelScope.launch {
        val email = SessionManager.emailFlow(getApplication()).firstOrNull()

        if (email.isNullOrBlank()) {
            // Si no hay sesión, llamamos a logout para poner el estado en "no logueado"
            logoutAndClearState()
            return@launch
        }

        // Buscamos al usuario en la base de datos
        val user = userDao.getByEmail(email)
        if (user == null) {
            // La sesión es inválida (usuario borrado?), deslogueamos
            logoutAndClearState()
            return@launch
        }

        // ¡Éxito! Configuramos el estado inicial con el nombre de usuario real
        uiState = CasinoUiState(
            isLoggedIn = true,
            profile = UserProfile(
                nombre = user.username, // <--- Usamos el username de la BD
                nivel = 1,              // Valor inicial para nivel
                xpActual = 0            // Valor inicial para XP
            ),
            balance = user.balance,
            statusMessage = "¡Bienvenido, ${user.username}!" // Mensaje de bienvenida real
        )
        // (Opcional) Podrías guardar user.id en currentUserId si lo necesitas luego.
        // currentUserId = user.id
    }

    // Renombramos a "logout" para que sea llamado desde la UI
    /**
     * Cierra sesión:
     * - Limpia SessionManager (DataStore).
     * - Resetea el estado a no logueado.
     */
    fun logout() = viewModelScope.launch {
        SessionManager.clear(getApplication()) // Limpia el DataStore
        uiState = CasinoUiState(statusMessage = "Sesión cerrada.", isLoggedIn = false)
    }

    // Función interna para limpiar el estado sin llamar a SessionManager de nuevo
    private fun logoutAndClearState() {
        uiState = CasinoUiState(statusMessage = "Sesión cerrada.", isLoggedIn = false)
    }

    /** Borra el mensaje de estado para no repetirlo en la UI. */
    fun consumeMessage() {
        uiState = uiState.copy(statusMessage = null)
    }

    /** Helper para publicar un mensaje a la UI (snackbar/banner). */
    private fun showMessage(msg: String) {
        uiState = uiState.copy(statusMessage = msg)
    }

    /**
     * Aplica un cambio de saldo y actualiza historial/mensaje:
     * - balance: asegura que nunca baje de 0.
     * - history: mantiene solo los últimos HISTORY_LIMIT registros.
     * - statusMessage: feedback corto de la acción.
     * - Persiste balance nuevo en Room si currentUserId está seteado.
     */
    private fun push(delta: Int, description: String) {
        val newBalance = max(0, uiState.balance + delta)
        val newHistory = (listOf(description) + uiState.history).take(HISTORY_LIMIT)
        uiState = uiState.copy(
            balance = newBalance,
            statusMessage = description,
            history = newHistory
        )
        if (currentUserId != 0L) {
            viewModelScope.launch {
                userDao.updateBalance(currentUserId, newBalance)
            }
        }
    }

    /** Regla simple para validar montos de apuesta contra el saldo. */
    private fun canBet(amount: Int): Boolean = amount in 1..uiState.balance

    /** Deposita dinero en la cuenta del jugador. */
    fun deposit(amount: Int) {
        if (amount <= 0) {
            showMessage("Monto de depósito inválido.")
            return
        }
        push(amount, "Depósito realizado +$amount")
    }

    /** Retira dinero si el monto es válido y hay saldo suficiente. */
    fun withdraw(amount: Int) {
        if (!canBet(amount)) {
            showMessage("Monto de retiro inválido o saldo insuficiente.")
            return
        }
        push(-amount, "Retiro realizado -$amount")
    }

    //  Función para el Bono Diario
    /** Recompensa un bono fijo y da experiencia por lealtad. */
    fun claimDailyBonus() {
        push(BONUS_AMOUNT, "Bono diario reclamado +$BONUS_AMOUNT")
        addExperience(50) // 50 XP por lealtad
    }

    /** Ejecuta una jugada de Ruleta y actualiza estado/experiencia. */
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
        addExperience(5)
    }

    /** Ejecuta una jugada de Slots y actualiza estado/experiencia. */
    fun playSlots(bet: Int) {
        if (!canBet(bet)) {
            showMessage("Saldo insuficiente o apuesta inválida.")
            return
        }
        val result: GameResult = repo.playSlots(bet)
        push(result.delta, result.description)
        uiState = uiState.copy(slotResults = result.slotResults)
        addExperience(15)
    }

    // --- BLACKJACK ---

    private var blackjackBet = 0 // Guarda la apuesta de la mano en curso

    /** Inicia una mano: valida saldo y reparte 2 cartas a jugador y crupier. */
    fun startBlackjack(bet: Int) {
        if (!canBet(bet)) {
            showMessage("Saldo insuficiente o apuesta inválida.")
            return
        }
        blackjackBet = bet
        val playerHand = listOf(repo.drawCard(), repo.drawCard())
        val dealerHand = listOf(repo.drawCard(), repo.drawCard())

        // Si es blackjack directo, terminamos el turno; si no, habilitamos acciones del jugador.
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

    /** Acción “Hit”: añade carta y evalúa si se pasó de 21. */
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

    /** Acción “Stand”: cede turno al crupier y cierra la mano. */
    fun blackjackStand() {
        if (!uiState.blackjackState.isPlayerTurn) return
        endBlackjackTurn(uiState.blackjackState.playerHand, uiState.blackjackState.dealerHand)
    }

    /**
     * Cierra la mano:
     * - El crupier roba hasta sumar 17 o más.
     * - Compara totales y determina delta/mensaje.
     * - Actualiza estado y da experiencia según el resultado.
     * @param customMessage si viene, fuerza el resultado (caso “te pasaste”).
     */
    private fun endBlackjackTurn(playerHand: List<Int>, dealerHand: List<Int>, customMessage: String? = null) {
        var currentDealerHand = dealerHand
        while (handTotal(currentDealerHand) < 17) {
            currentDealerHand = currentDealerHand + repo.drawCard()
        }

        val playerTotal = handTotal(playerHand)
        val dealerTotal = handTotal(currentDealerHand)

// ...
        val resultMessage: String
        val delta: Int

        if (customMessage != null) {
            // customMessage es "¡Te has pasado! Gana la casa."
            resultMessage = "¡Perdió $blackjackBet! (Te has pasado)"
            delta = -blackjackBet
        } else if (playerTotal > 21) {
            resultMessage = "¡Perdió $blackjackBet! (Te has pasado de 21)"
            delta = -blackjackBet
        } else if (dealerTotal > 21) {
            resultMessage = "¡Ganó $blackjackBet! (Crupier se pasó)"
            delta = blackjackBet
        } else if (playerTotal > dealerTotal) {
            resultMessage = "¡Ganó $blackjackBet! (Mano mayor)"
            delta = blackjackBet
        } else if (dealerTotal > playerTotal) {
            resultMessage = "¡Perdió $blackjackBet! (Mano menor)"
            delta = -blackjackBet
        } else {
            resultMessage = "¡Empate! (Push)"
            delta = 0
        }
// ...

        // ---  Añadir XP  ---
        if (delta > 0) { // Ganó
            addExperience(20)
        } else if (delta < 0) { // Perdió
            addExperience(5)
        } else { // Empate
            addExperience(2)
        }

        // Publica resultado y cierra turno del jugador
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

    /** Suma de cartas considerando As (11) que puede bajar a 1 si se pasa de 21. */
    private fun handTotal(cards: List<Int>): Int {
        var total = cards.sum()
        var aces = cards.count { it == 11 }
        while (total > 21 && aces > 0) {
            total -= 10
            aces--
        }
        return total
    }

    // --- función de Experiencia (XP) ---
    /**
     * Sube experiencia del perfil y ajusta nivel:
     * - XP requerido por nivel: nivel * 100.
     * - Soporta subir múltiples niveles si acumula suficiente XP.
     */
    private fun addExperience(xpAmount: Int) {

        val currentProfile = uiState.profile
        if (currentProfile == null) return

        // Calculamos el XP necesario basado en el nivel actual (ej: Nivel 1 -> 100 XP, Nivel 2 -> 200 XP)
        val xpParaSiguienteNivel = currentProfile.nivel * 100

        var nuevoXpTotal = currentProfile.xpActual + xpAmount
        var nuevoNivel = currentProfile.nivel

        // Bucle por si sube varios niveles de golpe (raro, pero posible)
        while (nuevoXpTotal >= xpParaSiguienteNivel) {
            nuevoNivel += 1
            nuevoXpTotal -= xpParaSiguienteNivel
        }

        val nuevoPerfil = currentProfile.copy(
            nivel = nuevoNivel,
            xpActual = nuevoXpTotal
        )

        uiState = uiState.copy(profile = nuevoPerfil)
    }
}
