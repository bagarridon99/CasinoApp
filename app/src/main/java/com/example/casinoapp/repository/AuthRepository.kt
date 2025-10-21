package com.example.casinoapp.repository

import com.example.casinoapp.data.AppDatabase
import com.example.casinoapp.data.PasswordHasher
import com.example.casinoapp.data.entity.UserEntity

sealed class AuthResult {
    data class Ok(val email: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(private val db: AppDatabase) {

    suspend fun login(email: String, password: String): AuthResult {
        val user = db.userDao().getByEmail(email)
            ?: return AuthResult.Error("Usuario no encontrado")

        val ok = PasswordHasher.verify(password, user.passwordHash, user.passwordSalt)
        return if (ok) AuthResult.Ok(email) else AuthResult.Error("Contraseña incorrecta")
    }

    // <--- FIRMA ACTUALIZADA ---
    suspend fun register(username: String, email: String, password: String): AuthResult {

        // <--- VALIDACIÓN AÑADIDA ---
        val existingUser = db.userDao().getByUsername(username)
        if (existingUser != null) return AuthResult.Error("El nombre de usuario ya está en uso")
        // <--- FIN VALIDACIÓN ---

        val existing = db.userDao().getByEmail(email)
        if (existing != null) return AuthResult.Error("El email ya está registrado")

        val (hash, salt) = PasswordHasher.hash(password)
        val id = db.userDao().insert(
            // <--- USERNAME AÑADIDO ---
            UserEntity(username = username, email = email, passwordHash = hash, passwordSalt = salt, balance = 0)
        )
        return if (id > 0) AuthResult.Ok(email) else AuthResult.Error("No se pudo crear el usuario")
    }
}