@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class
)

package com.example.casinoapp.view

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.casinoapp.R
import com.example.casinoapp.ui.common.BouncyButton
import com.example.casinoapp.ui.common.CasinoBackground
import com.example.casinoapp.ui.common.ImageLogo
import com.example.casinoapp.ui.common.RouletteProgress
import com.example.casinoapp.ui.common.Twinkles
import com.example.casinoapp.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Gold = Color(0xFFFFD700)
private val DarkGlass = Color(0xFF121212).copy(alpha = 0.90f)

@Composable
fun LoginScreen(
    snackbarHostState: SnackbarHostState,
    onLogin: (String, String) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val app = LocalContext.current.applicationContext as Application
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(app) as T
    })
    val state by vm.state.collectAsState()

    var user by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val focus = LocalFocusManager.current
    val kb = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(user).matches()
    val passValid = pass.length >= 6

    var showCard by remember { mutableStateOf(false) }
    var showFields by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showCard = true
        delay(120)
        showFields = true
    }

    val shake = remember { Animatable(0f) }
    LaunchedEffect(state.msg) {
        state.msg?.let { snackbarHostState.showSnackbar(it) }
        if (state.msg != null) {
            listOf(0f, -12f, 10f, -8f, 6f, -3f, 0f).forEach { shake.animateTo(it, tween(60)) }
        }
    }

    // --- CONFIGURACIÓN DE COLORES PARA TEXTO BLANCO PURO ---
    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Gold,
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = Gold,
        unfocusedLabelColor = Color.White,
        cursorColor = Gold,
        // ESTO ASEGURA QUE LO QUE ESCRIBES SEA BLANCO
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        // Fondo oscuro para que el texto blanco resalte
        focusedContainerColor = Color.Black.copy(0.85f),
        unfocusedContainerColor = Color.Black.copy(0.75f)
    )

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CasinoBackground()
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Twinkles(Modifier.size(280.dp), count = 10)
                    ImageLogo(Modifier.padding(top = 4.dp))
                }

                Spacer(Modifier.height(32.dp))

                AnimatedVisibility(visible = showCard, enter = fadeIn() + slideInVertically { it / 12 }) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkGlass),
                        border = BorderStroke(1.dp, Color.White.copy(0.1f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { translationX = shake.value }
                    ) {
                        Column(
                            Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Iniciar Sesión", style = MaterialTheme.typography.headlineSmall, color = Color.White)

                            AnimatedVisibility(visible = showFields) {
                                OutlinedTextField(
                                    value = user,
                                    onValueChange = { user = it },
                                    label = { Text("Email") },
                                    singleLine = true,
                                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp), // Refuerzo de color blanco
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = user.isNotBlank() && !emailValid,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = tfColors
                                )
                            }

                            AnimatedVisibility(visible = showFields) {
                                OutlinedTextField(
                                    value = pass,
                                    onValueChange = { pass = it },
                                    label = { Text("Contraseña") },
                                    singleLine = true,
                                    textStyle = TextStyle(color = Color.White, fontSize = 16.sp), // Refuerzo de color blanco
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                                contentDescription = null,
                                                tint = Gold
                                            )
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        if (emailValid && passValid && !state.loading) {
                                            kb?.hide(); focus.clearFocus(); vm.login(user, pass)
                                        }
                                    }),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = tfColors
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            BouncyButton(
                                enabled = emailValid && passValid && !state.loading,
                                onClick = { kb?.hide(); focus.clearFocus(); vm.login(user, pass) },
                                modifier = Modifier.fillMaxWidth().height(52.dp)
                            ) {
                                if (state.loading) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RouletteProgress(modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Cargando...", color = Color.Black)
                                    }
                                } else {
                                    Text("INGRESAR", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                                }
                            }

                            TextButton(onClick = onNavigateToSignUp) {
                                Text("¿No tienes cuenta? ", color = Color.White.copy(0.7f))
                                Text("Regístrate", color = Gold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.email != null) LaunchedEffect(Unit) { onLogin(user, pass) }
}