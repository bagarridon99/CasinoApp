package com.example.casinoapp.ui.common

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// Importa utilidades desde RouletteMath.kt
// (evita duplicados que te estaban chocando)
import com.example.casinoapp.ui.common.EURO_WHEEL_ORDER
import com.example.casinoapp.ui.common.SLOT_DEG
import com.example.casinoapp.ui.common.angleForNumber
import com.example.casinoapp.ui.common.forwardDelta
import com.example.casinoapp.ui.common.norm360

@Composable
fun RouletteWheel(
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    baseOffsetDeg: Float = -SLOT_DEG / 2f, // alinear separadores con puntero 12:00
    targetNumber: Int?,
    spinTrigger: Int,
    onSpinEnd: (() -> Unit)? = null
) {
    val wheelAngle = remember { Animatable(0f) } // rotación del disco (grados)
    val ballAngle  = remember { Animatable(0f) } // órbita de la bola (grados)
    val scope = rememberCoroutineScope()

    LaunchedEffect(spinTrigger) {
        if (targetNumber == null) return@LaunchedEffect

        val fast = tween<Float>(durationMillis = 900, easing = LinearEasing)
        val mid  = tween<Float>(durationMillis = 700, easing = LinearEasing)
        val slow = tween<Float>(durationMillis = 1200, easing = CubicBezierEasing(0.1f, 0.0f, 0.2f, 1.0f))

        // Fase rápida
        scope.launch { wheelAngle.animateTo(wheelAngle.value + 360f * 6f, animationSpec = fast) }
        scope.launch { ballAngle.animateTo(ballAngle.value - 360f * 8f,  animationSpec = fast) }.join()

        // Fase media
        scope.launch { wheelAngle.animateTo(wheelAngle.value + 360f * 2f, animationSpec = mid) }
        scope.launch { ballAngle.animateTo(ballAngle.value - 360f * 2.5f, animationSpec = mid) }.join()

        // Frenado con snap exacto al ganador
        val targetAngle = angleForNumber(targetNumber, baseOffsetDeg)
        val deltaWheel  = forwardDelta(wheelAngle.value, targetAngle)
        val wheelFinal  = wheelAngle.value + deltaWheel + 360f * 0.5f
        val ballFinal   = ballAngle.value - (deltaWheel + 360f * 0.6f)

        scope.launch { wheelAngle.animateTo(wheelFinal, animationSpec = slow) }
        scope.launch { ballAngle.animateTo(ballFinal,  animationSpec = slow) }.join()

        onSpinEnd?.invoke()
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius   = size.toPx() / 2f
            val ringOut  = radius * 0.98f
            val ringIn   = radius * 0.52f // deja centro libre
            val orbit    = radius * 0.72f // órbita de la bola

            val outerRect = rectFromCenter(center, ringOut)
            val innerRect = rectFromCenter(center, ringIn)

            // Pintura nativa para los números
            val textPx = size.toPx() * 0.07f
            val textPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.WHITE
                textAlign = Paint.Align.CENTER
                textSize = textPx
            }
            val strokePaint = Paint(textPaint).apply {
                style = Paint.Style.STROKE
                strokeWidth = textPx * 0.12f
                color = android.graphics.Color.BLACK
            }

            // Disco con segmentos + números (rota con wheelAngle)
            rotate(degrees = wheelAngle.value % 360f, pivot = center) {
                EURO_WHEEL_ORDER.forEachIndexed { idx, number ->
                    val start = -90f + baseOffsetDeg + idx * SLOT_DEG
                    val sweep = SLOT_DEG

                    val sectorColor: Color = when {
                        number == 0 -> Color(0xFF008000) // verde
                        number in setOf(
                            1, 3, 5, 7, 9, 12, 14, 16, 18,
                            19, 21, 23, 25, 27, 30, 32, 34, 36
                        ) -> Color(0xFFB71C1C) // rojo
                        else -> Color.Black
                    }

                    // Sector exterior
                    drawArc(
                        color = sectorColor,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(outerRect.left, outerRect.top),
                        size = outerRect.size
                    )
                    // “Tapa” interior
                    drawArc(
                        color = Color(0xFF2B2B2B),
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(innerRect.left, innerRect.top),
                        size = innerRect.size
                    )

                    // Número centrado
                    val mid = start + sweep / 2f
                    val rad = Math.toRadians(mid.toDouble())
                    val labelR = (ringOut + ringIn) / 2f
                    val tx = center.x + (labelR * cos(rad)).toFloat()
                    val ty = center.y + (labelR * sin(rad)).toFloat()

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(number.toString(), tx, ty + textPx * 0.35f, strokePaint)
                        drawText(number.toString(), tx, ty + textPx * 0.35f, textPaint)
                    }
                }
            }

            // Bola (órbita contraria)
            val a = norm360(ballAngle.value)
            val rad = Math.toRadians(a.toDouble())
            val cx = center.x + (orbit * sin(rad)).toFloat()
            val cy = center.y - (orbit * cos(rad)).toFloat()

            // sombra + bola
            drawCircle(color = Color(0x22000000), radius = 12f, center = Offset(cx + 2f, cy + 2f))
            drawCircle(color = Color.White,       radius = 8f,  center = Offset(cx, cy))
        }
    }
}

private fun rectFromCenter(center: Offset, radius: Float): Rect =
    Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius)
