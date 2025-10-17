package com.example.casinoapp.model

sealed class RouletteBet {
    data class ByColor(val color: RouletteColor) : RouletteBet()
    data class ByNumber(val number: Int) : RouletteBet()
}