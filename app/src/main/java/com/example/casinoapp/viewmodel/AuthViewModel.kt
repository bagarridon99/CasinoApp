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

/**
 * Estado observable para la pantalla de autenticación.
 * - loading: muestra progreso mientras se consulta el repositorio
 * - email: email autenticado (cuando el login fue OK)
 * - msg: mensaje de feedback para snackbar/diálogo (errores o avisos)
 */
data class AuthState(
    val loading: Boolean = false,
    val email: String? = null,
    val msg: String? = null
)

/**
 * ViewModel para autenticación (Login/Registro).
 * - Mantiene un AuthState reactivo (StateFlow) para la UI.
 * - Orquesta llamadas al AuthRepository y persiste sesión con DataStore.
 */
class AuthViewModel(app: Application) : AndroidViewModel(app) {

    // Repositorio de auth, inyectado con la instancia de Room
    private val repo = AuthRepository(AppDatabase.get(app))

    // StateFlow interno y público (expuesto como solo-lectura)
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    /**
     * Intenta iniciar sesión con email y password.
     * 1) Pone loading = true.
     * 2) Llama al repo.login().
     * 3) Si es OK: guarda email en SessionManager (DataStore) y notifica éxito.
     * 4) Si es Error: muestra el mensaje devuelto por el repositorio.
     */
    fun login(email: String, pass: String) = viewModelScope.launch {
        _state.update { it.copy(loading = true, msg = null) }
        when (val r = repo.login(email, pass)) {
            is AuthResult.Ok -> {
                // Persistimos sesión (email) para que el resto de la app lo consuma
                SessionManager.setEmail(getApplication(), r.email)
                _state.update { it.copy(loading = false, email = r.email, msg = null) }
            }
            is AuthResult.Error -> {
                _state.update { it.copy(loading = false, msg = r.message) }
            }
        }
    }

    // <--- FIRMA ACTUALIZADA ---
    /**
     * Registra un nuevo usuario:
     * 1) Muestra loading.
     * 2) Llama a repo.register(username, email, pass).
     * 3) Si es OK: no inicia sesión automáticamente; avisa a la UI que inicie sesión.
     * 4) Si es Error: expone el mensaje del repositorio.
     */
    fun register(username: String, email: String, pass: String) = viewModelScope.launch {
        _state.update { it.copy(loading = true, msg = null) }

        // <--- LLAMADA ACTUALIZADA ---
        when (val r = repo.register(username, email, pass)) {
            is AuthResult.Ok -> {
                _state.update { it.copy(loading = false, msg = "Usuario creado. Inicia sesión para continuar.") }
            }
            is AuthResult.Error -> {
                _state.update { it.copy(loading = false, msg = r.message) }
            }
        }
    }

    /** Consume (borra) el mensaje actual para no volver a mostrarlo. */
    fun consumeMessage() {
        _state.update { it.copy(msg = null) }
    }

    /**
     * Cierra sesión:
     * - Limpia email en DataStore.
     * - Resetea email en el estado (UI puede navegar a Login).
     */
    fun logout() = viewModelScope.launch {
        SessionManager.clear(getApplication())
        _state.update { it.copy(email = null) }
    }
}
