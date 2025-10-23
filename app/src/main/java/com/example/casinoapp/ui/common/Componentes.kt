package com.example.casinoapp.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.casinoapp.R
import androidx.compose.animation.animateColorAsState

/* ===========================
 * Logo (Faltaba)
 * =========================== */
@Composable
fun ImageLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.logo_casino),
        contentDescription = "Logo CasinoApp",
        contentScale = ContentScale.Fit,
        modifier = modifier.size(190.dp) // Tamaño ajustado para login
    )
}

/* ===========================
 * Botón Bouncy (Faltaba)
 * =========================== */
@Composable
fun BouncyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bouncyScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        try {
                            awaitRelease()
                        } finally {
                            pressed = false
                        }
                    }
                )
            },
        content = content
    )
}

/* ===========================
 * Botón Bouncy Primario (Faltaba)
 * =========================== */
@Composable
fun BouncyPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    BouncyButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        content = {
            if (loading) {
                RouletteProgress(modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("Cargando...")
            } else {
                Text(text)
            }
        }
    )
}

/* ===========================
 * Loader Ruleta (Faltaba)
 * =========================== */
@Composable
fun RouletteProgress(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 3.dp
    )
}

/* ===========================
 * Checklist Contraseña (Faltaba)
 * =========================== */
@Composable
fun PasswordChecklistRow(
    hasLength: Boolean,
    hasUpper: Boolean,
    hasDigit: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = 4.dp, start = 8.dp)
    ) {
        RequirementRow(isValid = hasLength, text = "Mínimo 6 caracteres")
        RequirementRow(isValid = hasUpper, text = "Al menos una mayúscula")
        RequirementRow(isValid = hasDigit, text = "Al menos un número")
    }
}

@Composable
private fun RequirementRow(isValid: Boolean, text: String) {
    val color = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val iconAlpha by animateFloatAsState(targetValue = if (isValid) 1f else 0.5f, label = "reqAlpha")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer { alpha = iconAlpha }
        )
        Text(text, style = MaterialTheme.typography.bodySmall, color = color)
    }
}

/* ===========================
 * Barra Fuerza Contraseña (Faltaba)
 * =========================== */
@Composable
fun PasswordStrengthBar(score: Float) {
    val progress by animateFloatAsState(targetValue = score, label = "strength")
    val color by animateColorAsState(
        targetValue = when (score) {
            in 0f..0.34f -> MaterialTheme.colorScheme.error
            in 0.34f..0.67f -> Color(0xFFFFA000) // Naranja
            else -> Color(0xFF1B5E20) // Verde (CasinoGreen)
        },
        label = "strengthColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        LinearProgressIndicator(
            progress = progress,
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}