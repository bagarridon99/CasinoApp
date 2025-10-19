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

    suspend fun register(email: String, password: String): AuthResult {
        val existing = db.userDao().getByEmail(email)
        if (existing != null) return AuthResult.Error("El email ya está registrado")

        val (hash, salt) = PasswordHasher.hash(password)
        val id = db.userDao().insert(
            UserEntity(email = email, passwordHash = hash, passwordSalt = salt)
        )
        return if (id > 0) AuthResult.Ok(email) else AuthResult.Error("No se pudo crear el usuario")
    }
}
