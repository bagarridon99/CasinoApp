package com.example.casinoapp.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.casinoapp.R
import java.text.NumberFormat
import java.util.Locale

/* ===========================
 * Formato de moneda (CLP)
 * =========================== */
fun formatCLP(value: Int): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    return nf.format(value.toLong())
}

/* ===========================
 * Tarjeta estilo “Glass”
 * =========================== */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 10.dp,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
        ),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

/* ===========================
 * Header unificado de juegos
 * =========================== */
@Composable
fun GameHeader(imageRes: Int, balance: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null, // imagen de fondo decorativa
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Overlay para contraste
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.35f))
            )
            Text(
                "Saldo: ${formatCLP(balance)}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

/* ===========================
 * Banda de resultado
 * =========================== */
@Composable
fun ResultBanner(text: String, positive: Boolean) {
    val bg = if (positive) MaterialTheme.colorScheme.tertiaryContainer
    else MaterialTheme.colorScheme.errorContainer
    val fg = if (positive) MaterialTheme.colorScheme.onTertiaryContainer
    else MaterialTheme.colorScheme.onErrorContainer

    Surface(
        color = bg,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 2.dp
    ) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}


/* ===================================================================
 * FUNCIONES MOVIDAS (ANTES ERAN PRIVADAS EN LOGIN Y HOME)
 * =================================================================== */

/* ---- Twinkles (Movido desde HomeScreen) ---- */
@Composable
fun Twinkles(modifier: Modifier = Modifier, count: Int = 8) {
    val t = rememberInfiniteTransition(label = "twk")
    val delays = remember { List(count) { 150 * it } }
    val anims = delays.mapIndexed { i, d ->
        t.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400 + d, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "a$i"
        )
    }
    Box(
        modifier = modifier.drawBehind {
            val w = size.width; val h = size.height
            val points = listOf(
                Offset(w*0.18f, h*0.30f), Offset(w*0.82f, h*0.32f),
                Offset(w*0.12f, h*0.55f), Offset(w*0.88f, h*0.58f),
                Offset(w*0.35f, h*0.18f), Offset(w*0.65f, h*0.16f),
                Offset(w*0.25f, h*0.72f), Offset(w*0.75f, h*0.74f),
                Offset(w*0.50f, h*0.10f), Offset(w*0.50f, h*0.82f)
            ).take(count)

            points.forEachIndexed { i, p ->
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = anims[i].value * 0.85f),
                    radius = 5f,
                    center = p
                )
            }
        }
    )
}

/* ---- CasinoBackground (Movido desde LoginScreen) ---- */
@Composable
fun CasinoBackground() {
    // Efecto Ken Burns (zoom/pan suave y cíclico)
    val t = rememberInfiniteTransition(label = "kenburns")
    val scale by t.animateFloat(
        initialValue = 1.15f, targetValue = 1.30f,
        animationSpec = infiniteRepeatable(tween(13000, easing = LinearEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val offsetX by t.animateFloat(
        initialValue = -30f, targetValue = 30f,
        animationSpec = infiniteRepeatable(tween(13000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offsetX"
    )
    val offsetY by t.animateFloat(
        initialValue = 10f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(13000, easing = LinearEasing), RepeatMode.Reverse),
        label = "offsetY"
    )

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ruleta),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }
        )
        // Overlay oscuro para legibilidad de la tarjeta
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.60f),
                            Color.Black.copy(alpha = 0.40f),
                            Color.Black.copy(alpha = 0.60f)
                        )
                    )
                )
        )
    }
}