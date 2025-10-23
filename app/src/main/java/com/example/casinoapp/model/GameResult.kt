package com.example.casinoapp.model

/**
 * Resultado agnóstico de juego.
 * Se puede reutilizar para distintos minijuegos:
 * - description: texto para mostrar (ganaste/perdiste, detalle)
 * - delta: variación del saldo (positivo o negativo)
 * - winningNumber: resultado de ruleta (opcional)
 * - playerHand/dealerHand: estado de blackjack (opcional)
 * - slotResults: símbolos resultantes de slots (opcional)
 *
 * Al dejar campos opcionales, la misma clase sirve para componer
 * la UI sin crear múltiples tipos de resultados.
 */
data class GameResult(
    val description: String,
    val delta: Int,
    val winningNumber: Int? = null,
    val playerHand: List<Int> = emptyList(),
    val dealerHand: List<Int> = emptyList(),
    val slotResults: List<SlotSymbol> = emptyList()
)
