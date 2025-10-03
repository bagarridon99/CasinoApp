package com.example.casinoapp.repository

import com.example.casinoapp.model.GameResult
import com.example.casinoapp.model.RouletteColor

interface CasinoRepository {
    fun playRoulette(bet: Int, color: RouletteColor): GameResult
    fun playBlackjack(bet: Int): GameResult
    fun playSlots(bet: Int): GameResult
}
