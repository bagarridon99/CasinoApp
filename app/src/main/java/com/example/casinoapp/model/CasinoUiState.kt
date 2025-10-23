package com.example.casinoapp.model

/**
 * Estado global de la UI para la app del casino.
 * Centraliza:
 * - Sesión (isLoggedIn, playerName, balance)
 * - Mensajes y bitácora (statusMessage, history)
 * - Subestados por juego (ruleta, slots, blackjack)
 * - Perfil del jugador (UserProfile)
 */
data class CasinoUiState(
    val isLoggedIn: Boolean = false,            // ¿El usuario inició sesión?
    val playerName: String = "",                // Nombre mostrado en la UI
    val balance: Int = 0,                       // Saldo actual del jugador
    val statusMessage: String? = null,          // Mensaje informativo/feedback para la UI
    val history: List<String> = emptyList(),    // Historial simple de acciones/resultados

    // --- Estados específicos de juegos ---
    val rouletteState: RouletteGameState = RouletteGameState(),  // Estado de Ruleta
    val slotResults: List<SlotSymbol> = emptyList(),             // Últimos símbolos de Slots
    val blackjackState: BlackjackGameState = BlackjackGameState(), // Estado de Blackjack

    // --- Información de perfil ---
    val profile: UserProfile = UserProfile()    // Perfil básico del usuario
)

/**
 * Estado de una partida de Blackjack en curso.
 * - playerHand/dealerHand: cartas representadas como enteros (p. ej. 1..11/valores)
 * - isPlayerTurn: si le toca jugar al usuario
 * - gameMessage: feedback contextual (ganaste, te pasaste, turno del dealer, etc.)
 */
data class BlackjackGameState(
    val playerHand: List<Int> = emptyList(),
    val dealerHand: List<Int> = emptyList(),
    val isPlayerTurn: Boolean = false,
    val gameMessage: String? = null
)

/**
 * Estado de la Ruleta.
 * - winningNumber: último número ganador (0..36) o null si aún no hay resultado
 */
data class RouletteGameState(
    val winningNumber: Int? = null
)

/**
 * Datos básicos del perfil mostrado en la UI (gamificación ligera).
 * - nombre: display name
 * - nivel/xpActual: progresión del jugador
 */
data class UserProfile(
    val nombre: String = "Jugador",
    val nivel: Int = 1,
    val xpActual: Int = 0
)
