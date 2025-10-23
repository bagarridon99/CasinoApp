package com.example.casinoapp.view

// LOS IMPORTS FALTANTES (DEBES AGREGAR LOS ARCHIVOS)
import com.example.casinoapp.ui.common.BouncyButton
import com.example.casinoapp.ui.common.ImageLogo
import com.example.casinoapp.ui.common.RouletteProgress
// IMPORTS CORREGIDOS (MOVIDOS A UIKit.kt)
import com.example.casinoapp.ui.common.Twinkles
import com.example.casinoapp.ui.common.CasinoBackground

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.casinoapp.R
import com.example.casinoapp.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.ExperimentalComposeUiApi
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Pantalla de Login:
 * - Fondo animado tipo "Ken Burns" coherente con Home.
 * - Tarjeta con campos de email/contraseña y validación mínima.
 * - Botón con animación “bouncy” y loader tipo ruleta durante carga.
 * - Maneja snackbar + diálogo para feedback de errores.
 */
@OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    snackbarHostState: SnackbarHostState,
    onLogin: (String, String) -> Unit,    // callback al loguear con éxito
    onNavigateToSignUp: () -> Unit        // navegación a registro
) {
    // ViewModel de Auth con factory manual (requiere Application)
    val app = LocalContext.current.applicationContext as Application
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(app) as T
        }
    })
    val state by vm.state.collectAsState() // estado expuesto por el VM

    // Campos controlados + visibilidad de contraseña
    var user by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // IME/Focus para UX fluida
    val focus = LocalFocusManager.current
    val kb = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    // Validaciones mínimas (email regex del framework y largo de pass)
    val emailValid by remember(user) {
        mutableStateOf(android.util.Patterns.EMAIL_ADDRESS.matcher(user).matches())
    }
    val passValid by remember(pass) { mutableStateOf(pass.length >= 6) }

    // Aparición escalonada de la tarjeta y sus campos
    var showCard by remember { mutableStateOf(false) }
    var showFields by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showCard = true
        delay(120)
        showFields = true
    }

    // Efecto “shake” cuando llega un mensaje de error
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state.msg) {
        state.msg?.let { snackbarHostState.showSnackbar(it) }
        if (state.msg != null) {
            listOf(0f, -12f, 10f, -8f, 6f, -3f, 0f).forEach {
                shake.animateTo(it, tween(60))
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Fondo con Ken Burns + overlay oscuro (AHORA ES PÚBLICO)
            CasinoBackground()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo con “twinkles” de fondo (AHORA ES PÚBLICO)
                Box(contentAlignment = Alignment.Center) {
                    Twinkles(Modifier.size(300.dp), count = 10)
                    ImageLogo(Modifier.padding(top = 4.dp))
                }

                Spacer(Modifier.height(24.dp))

                // Tarjeta de login (glass) con animación de entrada
                AnimatedVisibility(
                    visible = showCard,
                    enter = fadeIn() + slideInVertically { it / 12 },
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { translationX = shake.value } // aplica shake en error
                    ) {
                        Column(
                            Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Paleta visual coherente para los TextFields
                            val tfColors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.20f),
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )

                            // Campo: email
                            AnimatedVisibility(
                                visible = showFields,
                                enter = fadeIn(animationSpec = tween(250, delayMillis = 0)) +
                                        slideInVertically { it / 10 }
                            ) {
                                OutlinedTextField(
                                    value = user,
                                    onValueChange = { user = it },
                                    label = { Text("Usuario (email)") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Next
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = user.isNotBlank() && !emailValid,
                                    supportingText = {
                                        if (user.isNotBlank() && !emailValid) Text("Ingresa un email válido")
                                    },
                                    colors = tfColors
                                )
                            }

                            // Campo: contraseña (con alternar visibilidad)
                            AnimatedVisibility(
                                visible = showFields,
                                enter = fadeIn(animationSpec = tween(250, delayMillis = 80)) +
                                        slideInVertically { it / 10 }
                            ) {
                                OutlinedTextField(
                                    value = pass,
                                    onValueChange = { pass = it },
                                    label = { Text("Contraseña") },
                                    singleLine = true,
                                    visualTransformation = if (passwordVisible)
                                        VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (emailValid && passValid && !state.loading) {
                                                kb?.hide(); focus.clearFocus()
                                                vm.login(user, pass)
                                            }
                                        }
                                    ),
                                    trailingIcon = {
                                        val desc = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = desc
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = pass.isNotBlank() && !passValid,
                                    supportingText = {
                                        if (pass.isNotBlank() && !passValid) Text("Mínimo 6 caracteres")
                                    },
                                    colors = tfColors
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            // Botón principal: “Ingresar”
                            BouncyButton(
                                enabled = emailValid && passValid && !state.loading,
                                onClick = {
                                    kb?.hide(); focus.clearFocus()
                                    vm.login(user, pass)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (state.loading) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RouletteProgress(modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Entrando a tu cuenta…")
                                    }
                                } else {
                                    Text("Ingresar")
                                }
                            }

                            // Navegación a registro
                            TextButton(onClick = onNavigateToSignUp) {
                                Text("¿No tienes cuenta? Regístrate aquí")
                            }

                            // Recuperación de contraseña: mensaje de demo
                            TextButton(onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "Recuperación de contraseña no disponible en modo local."
                                    )
                                }
                            }) {
                                Text("¿Olvidaste tu contraseña?")
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo opcional de feedback (además del snackbar)
    val showDialog = state.msg != null
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { vm.consumeMessage() },
            title = { Text("Atención") },
            text = { Text(state.msg ?: "") },
            confirmButton = {
                TextButton(onClick = { vm.consumeMessage() }) { Text("OK") }
            },
            // Atajo para llevar a SignUp si el error es “Usuario no encontrado”
            dismissButton = {
                if (state.msg?.contains("Usuario no encontrado", true) == true) {
                    TextButton(onClick = {
                        vm.consumeMessage(); onNavigateToSignUp()
                    }) { Text("Registrarme") }
                }
            }
        )
    }

    // Cuando el VM muestra email != null, consideramos login OK y navegamos.
    LaunchedEffect(state.email) {
        if (state.email != null) onLogin(user, pass)
    }
}
