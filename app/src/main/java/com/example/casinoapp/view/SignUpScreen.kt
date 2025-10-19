package com.example.casinoapp.view

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.casinoapp.R
import com.example.casinoapp.viewmodel.AuthViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
// IMPORTACIONES AÑADIDAS
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    snackbarHostState: SnackbarHostState,
    onSignUp: (String, String, String) -> Unit, // compat
    onBackToLogin: () -> Unit
) {
    // VM
    val app = LocalContext.current.applicationContext as Application
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(app) as T
        }
    })
    val state by vm.state.collectAsState()

    // Campos
    var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var passVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var ageConfirmed: Boolean? by rememberSaveable { mutableStateOf(null) }
    var acceptTerms by rememberSaveable { mutableStateOf(false) }

    // Validaciones
    val emailValid = remember(email) { android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    val passHasLen = pass.length >= 6
    val passHasUpper = pass.any { it.isUpperCase() }
    val passHasDigit = pass.any { it.isDigit() }
    val passStrong = passHasLen && passHasUpper && passHasDigit
    val confirmValid = confirm.isNotEmpty() && confirm == pass
    val ageOk = ageConfirmed == true
    val canCreate = emailValid && passStrong && confirmValid && ageOk && acceptTerms && !state.loading

    // Anim para la tarjeta (rebote sutil en error)
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state.msg) {
        state.msg?.let { snackbarHostState.showSnackbar(it) }
        if (state.msg != null) {
            listOf(0f, -12f, 10f, -8f, 6f, -3f, 0f).forEach { shake.animateTo(it, tween(60)) }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            // Fondo (igual al Login)
            SignUpBackground()

            // DECLARACIÓN DEL SCROLL STATE AÑADIDA
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .verticalScroll(scrollState), // MODIFICADOR AÑADIDO
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo + twinkles
                Box(contentAlignment = Alignment.Center) {
                    Twinkles(Modifier.size(260.dp), count = 8)
                    Image(
                        painter = painterResource(id = R.drawable.logo_casino),
                        contentDescription = "Logo CasinoApp",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(220.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Tarjeta “glass”
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { translationX = shake.value }
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text("Crear cuenta", style = MaterialTheme.typography.titleLarge)

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            singleLine = true,
                            isError = email.isNotEmpty() && !emailValid,
                            supportingText = {
                                if (email.isNotEmpty() && !emailValid) Text("Ingresa un email válido")
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = pass,
                            onValueChange = { pass = it },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passVisible = !passVisible }) {
                                    Icon(
                                        if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (passVisible) "Ocultar" else "Mostrar"
                                    )
                                }
                            },
                            isError = pass.isNotEmpty() && !passStrong,
                            supportingText = {
                                PasswordChecklistRow(passHasLen, passHasUpper, passHasDigit)
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        PasswordStrengthBar(
                            score = listOf(passHasLen, passHasUpper, passHasDigit).count { it } / 3f
                        )

                        OutlinedTextField(
                            value = confirm,
                            onValueChange = { confirm = it },
                            label = { Text("Confirmar contraseña") },
                            singleLine = true,
                            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                    Icon(
                                        if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (confirmVisible) "Ocultar" else "Mostrar"
                                    )
                                }
                            },
                            isError = confirm.isNotEmpty() && !confirmValid,
                            supportingText = {
                                if (confirm.isNotEmpty() && !confirmValid) Text("Las contraseñas no coinciden")
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Mayor de edad (chips)
                        Text("¿Eres mayor de 18 años?", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = ageConfirmed == true,
                                onClick = { ageConfirmed = true },
                                label = { Text("Sí") },
                                leadingIcon = {
                                    AnimatedVisibility(ageConfirmed == true) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                                    }
                                }
                            )
                            FilterChip(
                                selected = ageConfirmed == false,
                                onClick = { ageConfirmed = false },
                                label = { Text("No") }
                            )
                        }
                        if (ageConfirmed == false) {
                            Text(
                                "Debes ser mayor de 18.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Términos
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(checked = acceptTerms, onCheckedChange = { acceptTerms = it })
                            Text(
                                "Acepto los términos y confirmo que el uso es local (proyecto universitario).",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Botón con “bouncy” + gradiente
                        BouncyPrimaryButton(
                            text = if (state.loading) "Creando…" else "Crear cuenta",
                            enabled = canCreate,
                            loading = state.loading,
                            onClick = { vm.register(email, pass) }
                        )

                        TextButton(onClick = onBackToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Volver a iniciar sesión")
                        }
                    }
                }
            }
        }
    }

    // Diálogo de resultado
    val showDialog = state.msg != null
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { /* bloqueamos toque fuera */ },
            title = { Text("Registro") },
            text = { Text(state.msg ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    if (state.msg?.contains("Usuario creado", ignoreCase = true) == true) {
                        vm.logout()
                        onBackToLogin()
                    } else {
                        vm.consumeMessage()
                    }
                }) { Text("OK") }
            }
        )
    }
}

/* ----------------------- Helpers visuales ----------------------- */

@Composable
private fun PasswordChecklistRow(hasLen: Boolean, hasUpper: Boolean, hasDigit: Boolean) {
    Column {
        RequirementRow(ok = hasLen, text = "Mínimo 6 caracteres")
        RequirementRow(ok = hasUpper, text = "Al menos 1 mayúscula (A-Z)")
        RequirementRow(ok = hasDigit, text = "Al menos 1 número (0-9)")
    }
}

@Composable
private fun RequirementRow(ok: Boolean, text: String) {
    val color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(if (ok) "✓ " else "• ", color = color)
        Text(text, color = color, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PasswordStrengthBar(score: Float) {
    val progress = score.coerceIn(0f, 1f)
    val color = when {
        progress < 0.34f -> Color(0xFFD32F2F)
        progress < 0.67f -> Color(0xFFF9A825)
        else -> Color(0xFF388E3C)
    }
    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
        color = color,
        trackColor = color.copy(alpha = 0.25f)
    )
}

@Composable
private fun BouncyPrimaryButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "btnScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    pressed = true
                    try { tryAwaitRelease() } finally { pressed = false }
                })
            },
        shape = RoundedCornerShape(14.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text)
    }
}

/* --------- Fondo y twinkles (match con Login) --------- */

@Composable
private fun SignUpBackground() {
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

@Composable
private fun Twinkles(modifier: Modifier = Modifier, count: Int = 8) {
    val t = rememberInfiniteTransition(label = "twk")
    val delays = remember { List(count) { 150 * it } }
    val anims = delays.mapIndexed { i, d ->
        t.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400 + d),
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
                Offset(w*0.25f, h*0.72f), Offset(w*0.75f, h*0.74f)
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