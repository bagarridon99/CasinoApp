package com.example.casinoapp.ui.common

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// NOTA: Usamos las constantes (EURO_WHEEL_ORDER, SLOT_DEG)
// que ya están definidas en tu archivo RouletteMath.kt
// para evitar el error de "Conflicting declarations".

@Composable
fun RouletteWheel(
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    targetNumber: Int?,
    spinTrigger: Int,
    onSpinEnd: (() -> Unit)? = null
) {
    // Alineamos el centro del "slot" con la flecha de arriba (Top Center)
    val baseOffsetDeg = -SLOT_DEG / 2f

    val wheelAngle = remember { Animatable(0f) }

    LaunchedEffect(spinTrigger) {
        if (targetNumber == null) return@LaunchedEffect

        // 1. Calculamos dónde está el número en la rueda
        val index = EURO_WHEEL_ORDER.indexOf(targetNumber).coerceAtLeast(0)
        val angleOfNumber = (baseOffsetDeg + index * SLOT_DEG) % 360f

        // 2. CORRECCIÓN MATEMÁTICA:
        val targetRotation = (360f - angleOfNumber) % 360f

        // 3. Calculamos delta
        val current = wheelAngle.value % 360f
        val deltaToTarget = if (targetRotation >= current) {
            targetRotation - current
        } else {
            (360f - current) + targetRotation
        }

        // 4. Añadimos vueltas extra
        val finalTargetValue = wheelAngle.value + deltaToTarget + (360f * 5)

        // Animación suave
        wheelAngle.animateTo(
            targetValue = finalTargetValue,
            animationSpec = tween(
                durationMillis = 4000,
                easing = CubicBezierEasing(0.2f, 0.0f, 0.2f, 1.0f)
            )
        )

        onSpinEnd?.invoke()
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = size.toPx() / 2f

            // Dibujamos la rueda girada
            rotate(degrees = wheelAngle.value % 360f, pivot = center) {

                // Fondo base negro
                drawCircle(color = Color.Black, radius = radius)

                EURO_WHEEL_ORDER.forEachIndexed { idx, number ->
                    // Cada sector es un arco
                    val startAngle = -90f + baseOffsetDeg + (idx * SLOT_DEG)

                    val color = when {
                        number == 0 -> Color(0xFF006400) // Verde
                        number in setOf(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36) -> Color(0xFFB71C1C) // Rojo
                        else -> Color.Black // Negro
                    }

                    // Dibujo del sector
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = SLOT_DEG,
                        useCenter = true,
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(0f, 0f)
                    )

                    // Borde blanco fino
                    drawArc(
                        color = Color.White.copy(alpha = 0.2f),
                        startAngle = startAngle,
                        sweepAngle = SLOT_DEG,
                        useCenter = true,
                        style = Stroke(width = 1f),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(0f, 0f)
                    )

                    // Dibujo del Número
                    val midAngleRad = Math.toRadians((startAngle + SLOT_DEG / 2).toDouble())
                    val textRadius = radius * 0.85f
                    val x = center.x + (textRadius * cos(midAngleRad)).toFloat()
                    val y = center.y + (textRadius * sin(midAngleRad)).toFloat()

                    drawContext.canvas.nativeCanvas.save()
                    drawContext.canvas.nativeCanvas.rotate((startAngle + SLOT_DEG / 2 + 90), x, y)

                    drawContext.canvas.nativeCanvas.drawText(
                        number.toString(),
                        x,
                        y,
                        Paint().apply {
                            this.color = android.graphics.Color.WHITE
                            this.textSize = radius * 0.12f
                            this.textAlign = Paint.Align.CENTER
                            this.typeface = Typeface.DEFAULT_BOLD
                            this.isAntiAlias = true
                        }
                    )
                    drawContext.canvas.nativeCanvas.restore()
                }
            }

            // Decoración central
            drawCircle(color = Color(0xFFFFD700), radius = radius * 0.12f, center = center)
            drawCircle(color = Color(0xFFB8860B), radius = radius * 0.05f, center = center)
        }
    }
}