package com.example.casinoapp.view

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.casinoapp.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    snackbarHostState: SnackbarHostState,
    onSignUp: (String, String, String) -> Unit, // lo mantenemos por compatibilidad, no es necesario usarlo
    onBackToLogin: () -> Unit
) {
    // VM con Factory clásica
    val app = LocalContext.current.applicationContext as Application
    val vm: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(app) as T
        }
    })
    val state by vm.state.collectAsState()

    var email by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    val valid = email.isNotBlank() && pass.length >= 4 && pass == confirm

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Crear cuenta", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { vm.register(email, pass) },
                enabled = valid && !state.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.loading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text("Crear cuenta")
            }

            TextButton(onClick = onBackToLogin) {
                Text("Volver a iniciar sesión")
            }
        }
    }

    // Mostrar mensajes del VM como diálogo y volver al login si se creó
    val showDialog = state.msg != null
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { /* bloqueamos cierre por fuera; usa los botones */ },
            title = { Text("Registro") },
            text = { Text(state.msg ?: "") },
            confirmButton = {
                TextButton(onClick = {
                    // Si se creó el usuario, volvemos a login
                    if (state.msg?.contains("Usuario creado", ignoreCase = true) == true) {
                        onBackToLogin()
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
}
