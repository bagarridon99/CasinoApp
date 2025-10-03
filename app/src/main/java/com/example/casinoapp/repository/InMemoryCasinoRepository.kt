package com.example.casinoapp.repository

import com.example.casinoapp.model.GameResult
import com.example.casinoapp.model.RouletteColor
import com.example.casinoapp.model.SlotSymbol
import kotlin.random.Random

class InMemoryCasinoRepository : CasinoRepository {

    // --------- Ruleta ---------
    override fun playRoulette(bet: Int, color: RouletteColor): GameResult {
        val number = Random.nextInt(37) // 0..36
        val winning = when (number) {
            0 -> RouletteColor.VERDE
            else -> if (isRed(number)) RouletteColor.ROJO else RouletteColor.NEGRO
        }

        val delta = when (color) {
            RouletteColor.ROJO, RouletteColor.NEGRO ->
                if (color == winning) bet else -bet
            RouletteColor.VERDE ->
                if (winning == RouletteColor.VERDE) bet * 17 else -bet
        }

        val msg = "Ruleta: número $number (${winning.label}). " + resultText(delta)
        return GameResult(msg, delta)
    }

    private fun isRed(n: Int): Boolean {
        if (n == 0) return false
        val reds = setOf(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36)
        return n in reds
    }

    // --------- Blackjack ---------
    override fun playBlackjack(bet: Int): GameResult {
        val player = drawHand(hitUntil = 16, dealer = false)
        val dealer = drawHand(hitUntil = 16, dealer = true)

        val delta = when {
            player > 21 && dealer > 21 -> 0
            player > 21 -> -bet
            dealer > 21 -> bet
            player > dealer -> bet
            player < dealer -> -bet
            else -> 0
        }

        val msg = "Blackjack: Jugador $player vs Crupier $dealer. " + resultText(delta)
        return GameResult(msg, delta)
    }

    private fun drawHand(hitUntil: Int, dealer: Boolean): Int {
        val cards = mutableListOf(drawCard(), drawCard())
        var total = handTotal(cards)
        while (total <= hitUntil) {
            cards += drawCard()
            total = handTotal(cards)
            if (dealer && total == 17 && cards.contains(11)) break // 17 suave
        }
        return total
    }

    private fun handTotal(cards: List<Int>): Int {
        var t = cards.sum()
        var aces = cards.count { it == 11 }
        while (t > 21 && aces > 0) { t -= 10; aces-- }
        return t
    }

    private fun drawCard(): Int {
        val values = intArrayOf(2,3,4,5,6,7,8,9,10,10,10,10,11)
        return values.random()
    }

    // --------- Tragamonedas ---------
    override fun playSlots(bet: Int): GameResult {
        val pool = buildList {
            addAll(List(6) { SlotSymbol.CEREZA })
            addAll(List(5) { SlotSymbol.CAMPANA })
            addAll(List(4) { SlotSymbol.TREBOL })
            addAll(List(3) { SlotSymbol.DIAMANTE })
            addAll(List(2) { SlotSymbol.SIETE })
        }

        val r1 = pool.random(); val r2 = pool.random(); val r3 = pool.random()
        val delta = when {
            r1 == r2 && r2 == r3 -> bet * when (r1) {
                SlotSymbol.SIETE -> 10
                SlotSymbol.DIAMANTE -> 6
                SlotSymbol.TREBOL -> 5
                SlotSymbol.CAMPANA -> 4
                SlotSymbol.CEREZA -> 3
            }
            r1 == r2 || r2 == r3 || r1 == r3 -> bet * 2
            else -> -bet
        }

        val msg = "Tragamonedas: ${listOf(r1, r2, r3).joinToString(" | ") { it.label }}. " + resultText(delta)
        return GameResult(msg, delta)
    }

    // --------- Util ---------
    private fun resultText(delta: Int) = when {
        delta > 0 -> "Ganaste +$delta"
        delta < 0 -> "Perdiste $delta"
        else -> "Empate (sin cambios)"
    }
}
