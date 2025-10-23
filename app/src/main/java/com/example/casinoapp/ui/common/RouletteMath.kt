package com.example.casinoapp.ui.common

// --------- SOLO EN ESTE ARCHIVO: constantes y helpers de la ruleta ---------

// Orden europeo de números (37 casillas)
val EURO_WHEEL_ORDER = listOf(
    0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23,
    10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
)

const val SLOT_COUNT = 37
const val SLOT_DEG = 360f / SLOT_COUNT

fun indexOfNumber(number: Int): Int = EURO_WHEEL_ORDER.indexOf(number).coerceAtLeast(0)

/** Ángulo (grados) para el centro de la casilla [number] en el wheel. */
fun angleForNumber(number: Int, baseOffsetDeg: Float = 0f): Float {
    val idx = indexOfNumber(number)
    return (baseOffsetDeg + idx * SLOT_DEG) % 360f
}

/** Normaliza a [0, 360). */
fun norm360(deg: Float): Float {
    var v = deg % 360f
    if (v < 0f) v += 360f
    return v
}

/** Diferencia mínima positiva desde [current] hasta [target] avanzando en sentido horario. */
fun forwardDelta(current: Float, target: Float): Float {
    val c = norm360(current)
    val t = norm360(target)
    return if (t >= c) t - c else 360f - (c - t)
}

/** Número “bajo el puntero” para el ángulo actual del wheel. */
fun numberAtAngle(wheelAngleDeg: Float, baseOffsetDeg: Float = 0f): Int {
    val a = norm360(wheelAngleDeg - baseOffsetDeg)
    val idx = ((a / SLOT_DEG).toInt()) % SLOT_COUNT
    return EURO_WHEEL_ORDER[idx]
}
