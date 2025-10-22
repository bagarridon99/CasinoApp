package com.example.casinoapp.repository

import com.example.casinoapp.model.GameResult
import com.example.casinoapp.model.RouletteBet
import com.example.casinoapp.model.RouletteColor

/**
 * Contrato de la capa de dominio para los minijuegos del casino.
 * Permite intercambiar implementaciones (ej: in-memory vs. real backend)
 * sin tocar la UI ni los ViewModels.
 */
interface CasinoRepository {
    /** Ejecuta una jugada de ruleta con un monto y un tipo de apuesta. */
    fun playRoulette(betAmount: Int, bet: RouletteBet): GameResult

    /** Ejecuta una jugada de tragamonedas (slots). */
    fun playSlots(bet: Int): GameResult

    /** Extrae una carta para blackjack (valor ya normalizado). */
    fun drawCard(): Int
}
