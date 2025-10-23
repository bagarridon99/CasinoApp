package com.example.casinoapp.view

// IMPORTS FALTANTES (DEBES AGREGAR LOS ARCHIVOS)
import com.example.casinoapp.ui.common.PasswordChecklistRow
import com.example.casinoapp.ui.common.PasswordStrengthBar
import com.example.casinoapp.ui.common.BouncyPrimaryButton
// IMPORTS CORREGIDOS (MOVIDOS A UIKit.kt)
import com.example.casinoapp.ui.common.CasinoBackground
import com.example.casinoapp.ui.common.Twinkles

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
import androidx.compose.foundation.text.KeyboardOptions // <--- ASEGÚRATE DE TENER ESTA

/**
 * Pantalla de Registro:
 * - Valida email, username y una contraseña "fuerte" (largo+mayúscula+dígito).
 * - Pide confirmación de edad y aceptación de términos del proyecto.
 * - Usa el AuthViewModel para realizar el registro y mostrar mensajes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    snackbarHostState: SnackbarHostState,
    onSignUp: (String, String, String) -> Unit, // compat (no se usa directamente aquí)
    onBackToLogin: () -> Unit
) {
    // VM de autenticación
    val app = LocalContext.current.applicationContext as Application
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(app) as T
        }
    })
    val state by vm.state.collectAsState()

    // Campos del formulario
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var passVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var ageConfirmed: Boolean? by rememberSaveable { mutableStateOf(null) }
    var acceptTerms by rememberSaveable { mutableStateOf(false) }

    // Validaciones
    val emailValid = remember(email) { android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    val usernameValid = remember(username) { username.isNotBlank() }
    val passHasLen = pass.length >= 6
    val passHasUpper = pass.any { it.isUpperCase() }
    val passHasDigit = pass.any { it.isDigit() }
    val passStrong = passHasLen && passHasUpper && passHasDigit
    val confirmValid = confirm.isNotEmpty() && confirm == pass
    val ageOk = ageConfirmed == true
    val canCreate = emailValid && usernameValid && passStrong && confirmValid && ageOk && acceptTerms && !state.loading

    // Animación de "shake" cuando hay error (mensaje en VM)
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
            // Fondo animado coherente con Login (CORREGIDO)
            CasinoBackground()

            // Scroll para pantallas pequeñas
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .verticalScroll(scrollState),
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

                // Tarjeta “glass” con el formulario
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

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            singleLine = true,
                            isError = email.isNotEmpty() && !emailValid,
                            supportingText = {
                                if (email.isNotEmpty() && !emailValid) Text("Ingresa un email válido")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Username
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Nombre de usuario") },
                            singleLine = true,
                            isError = username.isNotEmpty() && !usernameValid,
                            supportingText = {
                                if (username.isNotEmpty() && !usernameValid) Text("El nombre de usuario no puede estar vacío")
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Contraseña + checklist de requisitos
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Barra de fuerza (visual)
                        PasswordStrengthBar(
                            score = listOf(passHasLen, passHasUpper, passHasDigit).count { it } / 3f
                        )

                        // Confirmación de contraseña
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
                            keyboardOptions = KeyboardOptions(
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

                        // Aceptación de términos del proyecto
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

                        // Botón principal (bouncy)
                        BouncyPrimaryButton(
                            text = if (state.loading) "Creando…" else "Crear cuenta",
                            enabled = canCreate,
                            loading = state.loading,
                            // Llama al VM con username + email + pass
                            onClick = { vm.register(username, email, pass) }
                        )

                        // Volver a Login
                        TextButton(onClick = onBackToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Volver a iniciar sesión")
                        }
                    }
                }
            }
        }
    }

    // Diálogo de resultado (registro OK o error)
    val showDialog = state.msg != null
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { /* bloqueamos toque fuera para guiar el flujo */ },
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