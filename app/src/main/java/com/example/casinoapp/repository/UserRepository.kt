package com.example.casinoapp.repository

import com.example.casinoapp.data.dao.UserDao
import com.example.casinoapp.data.entity.UserEntity
import com.example.casinoapp.data.security.PasswordHasher

class UserRepository(private val dao: UserDao) {

    suspend fun register(email: String, password: CharArray): Result<Long> {
        try {
            if (dao.getByEmail(email) != null) {
                return Result.failure(IllegalStateException("Email ya registrado"))
            }
            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hash(password, salt)
            val id = dao.insert(UserEntity(email = email, passwordHash = hash, passwordSalt = salt))
            return Result.success(id)
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            password.fill('*')
        }
    }

    suspend fun login(email: String, password: CharArray): Result<Unit> {
        try {
            val u = dao.getByEmail(email) ?: return Result.failure(NoSuchElementException("Usuario no encontrado"))
            val ok = PasswordHasher.verify(password, u.passwordSalt, u.passwordHash)
            return if (ok) Result.success(Unit) else Result.failure(SecurityException("Credenciales inválidas"))
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            password.fill('*')
        }
    }

    /** Genera código 6 dígitos con expiración (por defecto 15 minutos). */
    suspend fun requestReset(email: String, ttlMinutes: Long = 15): Result<String> {
        val user = dao.getByEmail(email) ?: return Result.failure(NoSuchElementException("Usuario no encontrado"))
        val code = (100000..999999).random().toString()
        val expires = System.currentTimeMillis() + ttlMinutes * 60_000
        dao.setRecovery(user.id, code, expires)
        return Result.success(code) // en producción: enviar por email/SMS
    }

    suspend fun resetPassword(email: String, code: String, newPassword: CharArray): Result<Unit> {
        try {
            val u = dao.getByEmail(email) ?: return Result.failure(NoSuchElementException("Usuario no encontrado"))
            val now = System.currentTimeMillis()
            if (u.recoveryCode == null || u.recoveryCodeExpiresAt == null) {
                return Result.failure(IllegalStateException("No hay solicitud"))
            }
            if (now > u.recoveryCodeExpiresAt) {
                return Result.failure(IllegalStateException("Código expirado"))
            }
            if (u.recoveryCode != code) {
                return Result.failure(IllegalStateException("Código inválido"))
            }
            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hash(newPassword, salt)
            dao.updatePassword(u.id, hash, salt)
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            newPassword.fill('*')
        }
    }
}