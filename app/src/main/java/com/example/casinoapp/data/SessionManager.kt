package com.example.casinoapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Crea un DataStore llamado "session" asociado al Context.
private val Context.dataStore by preferencesDataStore(name = "session")

/**
 * Maneja el almacenamiento persistente de la sesión del usuario.
 * Utiliza DataStore en lugar de SharedPreferences (más moderno y seguro).
 */
object SessionManager {
    // Clave única para guardar el email del usuario logueado.
    private val KEY_EMAIL = stringPreferencesKey("email")

    /**
     * Guarda el email del usuario actual en el DataStore.
     * Se usa al iniciar sesión o registrarse.
     */
    suspend fun setEmail(context: Context, email: String) {
        context.dataStore.edit { it[KEY_EMAIL] = email }
    }

    /**
     * Limpia los datos de sesión (por ejemplo, al cerrar sesión).
     */
    suspend fun clear(context: Context) {
        context.dataStore.edit { it.remove(KEY_EMAIL) }
    }

    /**
     * Devuelve un Flow reactivo con el email del usuario guardado.
     * Flow emite automáticamente los cambios (útil para observar sesión activa).
     */
    fun emailFlow(context: Context): Flow<String?> =
        context.dataStore.data.map { it[KEY_EMAIL] }
}
