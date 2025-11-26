@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.casinoapp.view

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.casinoapp.R
import com.example.casinoapp.ui.common.CasinoBackground
import com.example.casinoapp.ui.common.PasswordChecklistRow
import com.example.casinoapp.ui.common.PasswordStrengthBar
import com.example.casinoapp.ui.common.Twinkles
import com.example.casinoapp.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

private val Gold = Color(0xFFFFD700)
private val DarkGlass = Color(0xFF121212).copy(alpha = 0.9f)

@Composable
fun SignUpScreen(
    snackbarHostState: SnackbarHostState,
    onSignUp: (String, String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    val app = LocalContext.current.applicationContext as Application
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(app) as T
    })
    val state by vm.state.collectAsState()

    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var passVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var ageConfirmed by rememberSaveable { mutableStateOf(false) }
    var acceptTerms by rememberSaveable { mutableStateOf(false) }

    // Validaciones
    val emailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val usernameValid = username.isNotBlank()
    val passHasLen = pass.length >= 6
    val passHasUpper = pass.any { it.isUpperCase() }
    val passHasDigit = pass.any { it.isDigit() }
    val passStrong = passHasLen && passHasUpper && passHasDigit
    val confirmValid = confirm.isNotEmpty() && confirm == pass
    val canCreate = emailValid && usernameValid && passStrong && confirmValid && ageConfirmed && acceptTerms && !state.loading

    val shake = remember { Animatable(0f) }

    // --- CORRECCIÓN CRÍTICA AQUÍ ---
    LaunchedEffect(state.msg) {
        state.msg?.let { msg ->
            // Buscamos "creada" (femenino) o "Inicia" para asegurar que sea el mensaje de éxito
            if (msg.contains("creada", ignoreCase = true) || msg.contains("Inicia", ignoreCase = true)) {

                // 1. Limpiamos el mensaje para evitar repeticiones
                vm.consumeMessage()

                // 2. Navegamos inmediatamente
                onBackToLogin()

            } else {
                // Caso Error: Mostramos el mensaje y agitamos la tarjeta
                launch { snackbarHostState.showSnackbar(msg) }
                listOf(0f, -10f, 10f, 0f).forEach { shake.animateTo(it) }
            }
        }
    }

    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Gold, unfocusedBorderColor = Color.Gray,
        focusedLabelColor = Gold, unfocusedLabelColor = Color.LightGray,
        cursorColor = Gold, focusedTextColor = Color.White, unfocusedTextColor = Color.White,
        focusedContainerColor = Color.Black.copy(0.3f), unfocusedContainerColor = Color.Black.copy(0.3f)
    )

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            CasinoBackground()
            Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)))

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Twinkles(Modifier.size(200.dp), count = 6)
                    Image(
                        painter = painterResource(R.drawable.logo_casino),
                        contentDescription = null,
                        modifier = Modifier.size(160.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkGlass),
                    border = BorderStroke(1.dp, Color.White.copy(0.1f)),
                    modifier = Modifier.fillMaxWidth().graphicsLayer { translationX = shake.value }
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Crear cuenta", style = MaterialTheme.typography.titleLarge, color = Color.White)

                        OutlinedTextField(
                            value = email, onValueChange = { email = it },
                            label = { Text("Email") }, singleLine = true, colors = tfColors, modifier = Modifier.fillMaxWidth(),
                            isError = email.isNotEmpty() && !emailValid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                        )

                        OutlinedTextField(
                            value = username, onValueChange = { username = it },
                            label = { Text("Usuario") }, singleLine = true, colors = tfColors, modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
                        )

                        OutlinedTextField(
                            value = pass, onValueChange = { pass = it },
                            label = { Text("Contraseña") }, singleLine = true, colors = tfColors, modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = { IconButton(onClick = { passVisible = !passVisible }) { Icon(if (passVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = Gold) } },
                            supportingText = { PasswordChecklistRow(passHasLen, passHasUpper, passHasDigit) }
                        )

                        PasswordStrengthBar(score = listOf(passHasLen, passHasUpper, passHasDigit).count { it } / 3f)

                        OutlinedTextField(
                            value = confirm, onValueChange = { confirm = it },
                            label = { Text("Confirmar contraseña") }, singleLine = true, colors = tfColors, modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = { IconButton(onClick = { confirmVisible = !confirmVisible }) { Icon(if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = Gold) } },
                            isError = confirm.isNotEmpty() && !confirmValid
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = ageConfirmed, onCheckedChange = { ageConfirmed = it }, colors = CheckboxDefaults.colors(checkedColor = Gold, checkmarkColor = Color.Black, uncheckedColor = Color.Gray))
                            Text("Soy mayor de 18 años", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = acceptTerms, onCheckedChange = { acceptTerms = it }, colors = CheckboxDefaults.colors(checkedColor = Gold, checkmarkColor = Color.Black, uncheckedColor = Color.Gray))
                            Text("Acepto los términos", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                        }

                        Button(
                            onClick = { vm.register(username, email, pass) },
                            enabled = canCreate,
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, disabledContainerColor = Color.DarkGray),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            if (state.loading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                            } else {
                                Text("REGISTRARSE", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        TextButton(onClick = onBackToLogin, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("Volver a iniciar sesión", color = Gold)
                        }
                    }
                }
            }
        }
    }
}