package com.example.casinoapp.model

/**
 * Símbolos posibles del juego de tragamonedas (Slots).
 * - label: nombre legible para la UI
 * - emoji: representación visual rápida y divertida
 */
enum class SlotSymbol(val label: String, val emoji: String) {
    CEREZA("Cereza", "🍒"),
    CAMPANA("Campana", "🔔"),
    TREBOL("Trébol", "🍀"),
    DIAMANTE("Diamante", "💎"),
    SIETE("Siete", "7️⃣")
}
