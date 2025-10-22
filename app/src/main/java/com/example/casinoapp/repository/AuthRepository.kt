package com.example.casinoapp.repository

import com.example.casinoapp.data.AppDatabase
import com.example.casinoapp.data.PasswordHasher
import com.example.casinoapp.data.entity.UserEntity

/**
 * Resultado de autenticación:
 * - Ok: login/registro exitoso (devuelve el email).
 * - Error: mensaje descriptivo para mostrar en UI.
 */
sealed class AuthResult {
    data class Ok(val email: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Repositorio de autenticación:
 * - login(email, password): valida credenciales contra Room.
 * - register(username, email, password): crea usuario con hash+salt si no existe.
 */
class AuthRepository(private val db: AppDatabase) {

    /**
     * Inicia sesión verificando el hash almacenado con el password ingresado.
     */
    suspend fun login(email: String, password: String): AuthResult {
        val user = db.userDao().getByEmail(email)
            ?: return AuthResult.Error("Usuario no encontrado")

        // Verifica PBKDF2 (hash + salt) con la utilidad PasswordHasher
        val ok = PasswordHasher.verify(password, user.passwordHash, user.passwordSalt)
        return if (ok) AuthResult.Ok(email) else AuthResult.Error("Contraseña incorrecta")
    }

    /**
     * Registra un nuevo usuario:
     * 1) Valida que el username no exista.
     * 2) Valida que el email no esté registrado.
     * 3) Genera hash+salt y persiste el UserEntity con balance inicial = 0.
     */
    suspend fun register(username: String, email: String, password: String): AuthResult {

        // Evita duplicar usernames
        val existingUser = db.userDao().getByUsername(username)
        if (existingUser != null) return AuthResult.Error("El nombre de usuario ya está en uso")

        // Evita duplicar emails
        val existing = db.userDao().getByEmail(email)
        if (existing != null) return AuthResult.Error("El email ya está registrado")

        // Hash PBKDF2 + salt y guardado
        val (hash, salt) = PasswordHasher.hash(password)
        val id = db.userDao().insert(
            UserEntity(
                username = username,
                email = email,
                passwordHash = hash,
                passwordSalt = salt,
                balance = 0
            )
        )
        return if (id > 0) AuthResult.Ok(email) else AuthResult.Error("No se pudo crear el usuario")
    }
}
