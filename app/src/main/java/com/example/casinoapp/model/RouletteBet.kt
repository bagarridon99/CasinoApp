package com.example.casinoapp.model

/**
 * Tipo sellado (sealed) que modela una apuesta de Ruleta.
 * Ventajas:
 * - Patrón exhaustivo en when(): el compilador exige cubrir todos los casos.
 * - Flexibilidad para representar diferentes formas de apostar.
 *
 * Casos:
 * - ByColor: apuesta a un color (rojo/negro/verde).
 * - ByNumber: apuesta a un número específico (0..36).
 */
sealed class RouletteBet {

    /** Apuesta por color. */
    data class ByColor(val color: RouletteColor) : RouletteBet()

    /** Apuesta por número exacto. */
    data class ByNumber(val number: Int) : RouletteBet()
}
