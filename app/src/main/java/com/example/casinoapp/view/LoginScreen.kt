package com.example.casinoapp.view

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.casinoapp.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    snackbarHostState: SnackbarHostState,
    onLogin: (String, String) -> Unit,       // se dispara tras login OK
    onNavigateToSignUp: () -> Unit
) {
    // ViewModel con Factory clásica
    val app = LocalContext.current.applicationContext as Application
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(app) as T
        }
    })
    val state by vm.state.collectAsState()

    var user by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Casino Royale", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = user,
                onValueChange = { user = it },
                label = { Text("Usuario (email)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icon, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { vm.login(user, pass) },
                enabled = user.isNotBlank() && pass.isNotBlank() && !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.loading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text("Ingresar")
            }

            TextButton(onClick = onNavigateToSignUp) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }

            TextButton(
                onClick = {
                    if (user.isNotBlank()) vm.requestReset(user)
                    else {
                        // si quieres, podrías mostrar un dialogo simple aquí también
                    }
                }
            ) {
                Text("¿Olvidaste tu contraseña?")
            }
        }
    }

    // ==== ALERTDIALOG: muestra el mensaje del VM en una ventana flotante ====
    val showDialog = state.msg != null
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { vm.consumeMessage() },
            title = { Text("Atención") },
            text = { Text(state.msg ?: "") },
            confirmButton = {
                TextButton(onClick = { vm.consumeMessage() }) {
                    Text("OK")
                }
            },
            // Si el mensaje es "Usuario no encontrado", ofrece ir a Registro
            dismissButton = {
                if (state.msg?.contains("Usuario no encontrado", ignoreCase = true) == true) {
                    TextButton(onClick = {
                        vm.consumeMessage()
                        onNavigateToSignUp()
                    }) {
                        Text("Registrarme")
                    }
                }
            }
        )
    }

    // Navega cuando el login fue OK
    LaunchedEffect(state.email) {
        if (state.email != null) onLogin(user, pass)
    }
}
