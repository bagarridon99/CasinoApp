// FILE: app/src/main/java/com/example/casinoapp/data/session/SessionManager.kt
package com.example.casinoapp.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session")

object SessionManager {
    private val KEY_EMAIL = stringPreferencesKey("email")

    suspend fun setEmail(context: Context, email: String) {
        context.dataStore.edit { it[KEY_EMAIL] = email }
    }

    suspend fun clear(context: Context) {
        context.dataStore.edit { it.remove(KEY_EMAIL) }
    }

    fun emailFlow(context: Context): Flow<String?> =
        context.dataStore.data.map { it[KEY_EMAIL] }
}
