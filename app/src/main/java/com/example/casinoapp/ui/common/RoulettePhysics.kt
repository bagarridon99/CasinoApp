package com.example.casinoapp.ui.common

// --------- En este archivo: utilidades de física/animación. Nada duplicado ---------

object RoulettePhysics {

    /**
     * Calcula un ángulo final que aterriza en [winningNumber] añadiendo [extraSpins] vueltas completas
     * desde [currentAngle]. Usa helpers de RouletteMath.kt (no declara nada de nuevo).
     */
    fun targetAngleForWin(
        currentAngle: Float,
        winningNumber: Int,
        baseOffsetDeg: Float = 0f,
        extraSpins: Int = 3
    ): Float {
        val target = angleForNumber(winningNumber, baseOffsetDeg)
        val delta = forwardDelta(currentAngle, target)
        return currentAngle + delta + 360f * extraSpins
    }
}
