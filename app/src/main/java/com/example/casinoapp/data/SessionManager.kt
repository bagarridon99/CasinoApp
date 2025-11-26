package com.example.casinoapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

object SessionManager {
    private val KEY_TOKEN = stringPreferencesKey("jwt_token")
    private val KEY_USER_ID = longPreferencesKey("user_id")
    private val KEY_EMAIL = stringPreferencesKey("email")

    /**
     * Guarda toda la sesión (token, id de usuario y email).
     * Si no quieres usar token por ahora, simplemente no llames a esta función.
     */
    suspend fun setSession(
        context: Context,
        token: String,
        userId: Long,
        email: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = userId
            prefs[KEY_EMAIL] = email
        }
    }

    /**
     * Guarda solo el email del usuario.
     * Esta es la función que usa tu AuthViewModel: SessionManager.setEmail(...)
     */
    suspend fun setEmail(context: Context, email: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMAIL] = email
        }
    }

    /**
     * Limpia toda la sesión (logout).
     */
    suspend fun clear(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // Flujos de lectura de la sesión
    fun getToken(context: Context): Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_TOKEN] }

    fun getUserId(context: Context): Flow<Long?> =
        context.dataStore.data.map { prefs -> prefs[KEY_USER_ID] }

    fun getEmail(context: Context): Flow<String?> =
        context.dataStore.data.map { prefs -> prefs[KEY_EMAIL] }
}
