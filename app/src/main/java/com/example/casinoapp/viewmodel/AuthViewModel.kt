// FILE: app/src/main/java/com/example/casinoapp/viewmodel/AuthViewModel.kt
package com.example.casinoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.casinoapp.data.AppDatabase
import com.example.casinoapp.data.session.SessionManager
import com.example.casinoapp.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val loading: Boolean = false,
    val msg: String? = null,
    val email: String? = null
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserRepository(AppDatabase.get(app).userDao())

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    fun register(email: String, pass: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, msg = null)
        val r = repo.register(email, pass.toCharArray())
        _state.value = if (r.isSuccess) AuthState(msg = "Usuario creado", email = email)
        else AuthState(msg = r.exceptionOrNull()?.message)
    }

    fun login(email: String, pass: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, msg = null)
        val r = repo.login(email, pass.toCharArray())
        if (r.isSuccess) {
            SessionManager.setEmail(getApplication(), email)
            _state.value = AuthState(msg = "Login OK", email = email)
        } else {
            _state.value = AuthState(msg = r.exceptionOrNull()?.message)
        }
    }

    fun requestReset(email: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, msg = null)
        val r = repo.requestReset(email)
        _state.value = if (r.isSuccess) AuthState(msg = "Código: ${r.getOrNull()}")
        else AuthState(msg = r.exceptionOrNull()?.message)
    }

    fun reset(email: String, code: String, newPass: String) = viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, msg = null)
        val r = repo.resetPassword(email, code, newPass.toCharArray())
        _state.value = if (r.isSuccess) AuthState(msg = "Contraseña actualizada")
        else AuthState(msg = r.exceptionOrNull()?.message)
    }

    fun consumeMessage() { _state.value = _state.value.copy(msg = null) }

    // <<< NUEVO: restaurar sesión al abrir la app >>>
    fun loadSession() = viewModelScope.launch {
        SessionManager.emailFlow(getApplication()).collect { saved ->
            _state.value = if (saved.isNullOrBlank()) AuthState() else AuthState(email = saved)
        }
    }

    // <<< NUEVO: cerrar sesión >>>
    fun logout() = viewModelScope.launch {
        SessionManager.clear(getApplication())
        _state.value = AuthState(msg = "Sesión cerrada") // email = null -> UI vuelve a Login
    }
}
