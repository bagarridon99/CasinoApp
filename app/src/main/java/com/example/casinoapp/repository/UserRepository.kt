package com.example.casinoapp.repository

import com.example.casinoapp.data.AppDatabase
import com.example.casinoapp.data.PasswordHasher
import com.example.casinoapp.data.entity.UserEntity

class UserRepository(private val db: AppDatabase) {

    suspend fun findByEmail(email: String): UserEntity? =
        db.userDao().getByEmail(email)

    // <--- FIRMA ACTUALIZADA ---
    suspend fun create(username: String, email: String, password: String): Long {
        val (hash, salt) = PasswordHasher.hash(password)
        return db.userDao().insert(
            // <--- USERNAME AÑADIDO ---
            UserEntity(username = username, email = email, passwordHash = hash, passwordSalt = salt, balance = 0)
        )
    }

    suspend fun verify(email: String, password: String): Boolean {
        val user = db.userDao().getByEmail(email) ?: return false
        return PasswordHasher.verify(password, user.passwordHash, user.passwordSalt)
    }

    suspend fun changePassword(userId: Long, newPassword: String) {
        val (hash, salt) = PasswordHasher.hash(newPassword)
        db.userDao().updatePassword(userId, hash, salt)
    }
}