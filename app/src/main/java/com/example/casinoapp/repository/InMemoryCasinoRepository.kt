package com.example.casinoapp.repository

import com.example.casinoapp.model.GameResult
import com.example.casinoapp.model.RouletteBet
import com.example.casinoapp.model.RouletteColor
import com.example.casinoapp.model.SlotSymbol
import kotlin.random.Random

/**
 * Implementación en memoria de los juegos.
 * - Útil para desarrollo, pruebas locales y demo sin backend.
 * - La lógica de pagos y mensajes está contenida aquí.
 */
class InMemoryCasinoRepository : CasinoRepository {

    /**
     * Ruleta:
     * - Número ganador 0..36.
     * - Color: 0=VERDE, pares/ímpares según set "rojos".
     * - Paga: color (1:1), verde (17:1), número exacto (35:1).
     */
    override fun playRoulette(betAmount: Int, bet: RouletteBet): GameResult {
        val winningNumber = Random.nextInt(37) // 0..36
        val winningColor = when {
            winningNumber == 0 -> RouletteColor.VERDE
            isRed(winningNumber) -> RouletteColor.ROJO
            else -> RouletteColor.NEGRO
        }

        val delta: Int
        val betDescription: String

        when (bet) {
            is RouletteBet.ByColor -> {
                betDescription = "Apostaste al ${bet.color.label}"
                delta = when (bet.color) {
                    RouletteColor.ROJO, RouletteColor.NEGRO ->
                        if (bet.color == winningColor) betAmount else -betAmount
                    RouletteColor.VERDE ->
                        if (winningColor == RouletteColor.VERDE) betAmount * 17 else -betAmount
                }
            }
            is RouletteBet.ByNumber -> {
                betDescription = "Apostaste al número ${bet.number}"
                delta = if (bet.number == winningNumber) betAmount * 35 else -betAmount
            }
        }

        val msg = "Salió el $winningNumber ($winningColor). $betDescription. " + resultText(delta)
        return GameResult(msg, delta, winningNumber = winningNumber)
    }

    /**
     * Slots:
     * - Pool ponderado de símbolos (más cerezas que sietes).
     * - 3 rodillos aleatorios.
     * - Paga: triple símbolo (multiplicador por símbolo), doble (x2), nada (-apuesta).
     */
    override fun playSlots(bet: Int): GameResult {
        val pool = buildList {
            addAll(List(6) { SlotSymbol.CEREZA })
            addAll(List(5) { SlotSymbol.CAMPANA })
            addAll(List(4) { SlotSymbol.TREBOL })
            addAll(List(3) { SlotSymbol.DIAMANTE })
            addAll(List(2) { SlotSymbol.SIETE })
        }

        // Tres resultados independientes desde el pool ponderado
        val results = listOf(pool.random(), pool.random(), pool.random())

        // Cálculo de premio según coincidencias
        val delta = when {
            results[0] == results[1] && results[1] == results[2] -> bet * when (results[0]) {
                SlotSymbol.SIETE -> 10
                SlotSymbol.DIAMANTE -> 6
                SlotSymbol.TREBOL -> 5
                SlotSymbol.CAMPANA -> 4
                SlotSymbol.CEREZA -> 3
            }
            results[0] == results[1] || results[1] == results[2] || results[0] == results[2] -> bet * 2
            else -> -bet
        }

        val msg = "Tragamonedas: ${results.joinToString(" | ") { it.emoji }}. " + resultText(delta)
        return GameResult(msg, delta, slotResults = results)
    }

    /**
     * Roba una carta para Blackjack (11 representa As).
     * Jotas/Reinas/Reyes se normalizan como 10.
     */
    override fun drawCard(): Int {
        val values = intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10, 11) // 11 es As
        return values.random()
    }

    /** Determina si un número es "rojo" en ruleta europea. */
    private fun isRed(n: Int): Boolean {
        if (n == 0) return false
        val reds = setOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
        return n in reds
    }

    /** Mensaje corto derivado del delta para explicar el resultado. */
    private fun resultText(delta: Int) = when {
        delta > 0 -> "¡Ganó +$delta!"
        delta < 0 -> "¡Perdió $delta!"
        else -> "Empate"
    }
}
