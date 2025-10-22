package com.example.casinoapp.repository

import com.example.casinoapp.data.AppDatabase
import com.example.casinoapp.data.PasswordHasher
import com.example.casinoapp.data.entity.UserEntity

/**
 * Repositorio de usuarios (capa de datos):
 * - findByEmail: trae entidad Room.
 * - create: crea usuario con hash+salt.
 * - verify: compara password con hash almacenado.
 * - changePassword: actualiza hash+salt del usuario.
 */
class UserRepository(private val db: AppDatabase) {

    /** Busca un usuario por email (o null si no existe). */
    suspend fun findByEmail(email: String): UserEntity? =
        db.userDao().getByEmail(email)

    /**
     * Crea un nuevo usuario persistiendo hash+salt y balance inicial en 0.
     * Retorna el ID autogenerado.
     */
    suspend fun create(username: String, email: String, password: String): Long {
        val (hash, salt) = PasswordHasher.hash(password)
        return db.userDao().insert(
            UserEntity(
                username = username,
                email = email,
                passwordHash = hash,
                passwordSalt = salt,
                balance = 0
            )
        )
    }

    /**
     * Verifica credenciales:
     * - Obtiene el usuario por email y compara password con PBKDF2.
     */
    suspend fun verify(email: String, password: String): Boolean {
        val user = db.userDao().getByEmail(email) ?: return false
        return PasswordHasher.verify(password, user.passwordHash, user.passwordSalt)
    }

    /**
     * Cambia la contraseña generando un nuevo hash+salt y actualizando en BD.
     */
    suspend fun changePassword(userId: Long, newPassword: String) {
        val (hash, salt) = PasswordHasher.hash(newPassword)
        db.userDao().updatePassword(userId, hash, salt)
    }
}
