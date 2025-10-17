package com.example.casinoapp.repository

import com.example.casinoapp.model.GameResult
import com.example.casinoapp.model.RouletteBet
import com.example.casinoapp.model.RouletteColor

interface CasinoRepository {
    fun playRoulette(betAmount: Int, bet: RouletteBet): GameResult
    fun playSlots(bet: Int): GameResult
    fun drawCard(): Int
}
