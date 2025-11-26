package com.example.casinoapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.casinoapp.data.AppDatabase
import com.example.casinoapp.data.SessionManager
import com.example.casinoapp.repository.AuthRepository
import com.example.casinoapp.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthState(val loading: Boolean = false, val email: String? = null, val msg: String? = null)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AuthRepository(AppDatabase.get(app))
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun login(email: String, pass: String) = viewModelScope.launch {
        _state.update { it.copy(loading = true, msg = null) }
        when (val r = repo.login(email, pass)) {
            is AuthResult.Ok -> {
                SessionManager.setEmail(getApplication(), r.email)
                _state.update { it.copy(loading = false, email = r.email) }
            }
            is AuthResult.Error -> _state.update { it.copy(loading = false, msg = r.message) }
        }
    }

    fun register(username: String, email: String, pass: String) = viewModelScope.launch {
        _state.update { it.copy(loading = true, msg = null) }
        when (val r = repo.register(username, email, pass)) {
            is AuthResult.Ok -> _state.update { it.copy(loading = false, msg = "Cuenta creada. Inicia sesión.") }
            is AuthResult.Error -> _state.update { it.copy(loading = false, msg = r.message) }
        }
    }

    fun consumeMessage() { _state.update { it.copy(msg = null) } }
    fun logout() = viewModelScope.launch {
        SessionManager.clear(getApplication())
        _state.update { it.copy(email = null) }
    }
}