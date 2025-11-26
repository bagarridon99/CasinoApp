package com.example.casinoapp.repository

import com.example.casinoapp.data.AppDatabase
import com.example.casinoapp.data.PasswordHasher
import com.example.casinoapp.data.entity.UserEntity

sealed class AuthResult {
    data class Ok(val email: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(private val db: AppDatabase) {

    suspend fun login(email: String, pass: String): AuthResult {
        val user = db.userDao().getByEmail(email) ?: return AuthResult.Error("Usuario no encontrado")
        val isValid = PasswordHasher.verify(pass, user.passwordHash, user.passwordSalt)
        return if (isValid) AuthResult.Ok(email) else AuthResult.Error("Contraseña incorrecta")
    }

    suspend fun register(username: String, email: String, pass: String): AuthResult {
        if (db.userDao().getByEmail(email) != null) return AuthResult.Error("Email ya registrado")
        if (db.userDao().getByUsername(username) != null) return AuthResult.Error("Usuario ya existe")

        val (hash, salt) = PasswordHasher.hash(pass)
        val newUser = UserEntity(
            username = username, email = email, passwordHash = hash, passwordSalt = salt, balance = 1000
        )
        db.userDao().insert(newUser)
        return AuthResult.Ok(email)
    }
}