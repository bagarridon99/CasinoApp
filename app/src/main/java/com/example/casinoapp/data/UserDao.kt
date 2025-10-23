package com.example.casinoapp.data.dao

import androidx.room.*
import com.example.casinoapp.data.entity.UserEntity

/**
 * DAO (Data Access Object) que define las consultas SQL para la tabla "users".
 * Room genera automáticamente las implementaciones de estas funciones.
 */
@Dao
interface UserDao {

    /**
     * Inserta un nuevo usuario.
     * Si ya existe un conflicto (por ejemplo, mismo email o username), lanza error (ABORT).
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    /**
     * Obtiene un usuario a partir de su email.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    /**
     * Obtiene un usuario por su nombre de usuario (username).
     * Añadido para permitir autenticación o validación por nombre.
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    /**
     * Actualiza todos los campos del usuario (entidad completa).
     */
    @Update
    suspend fun update(user: UserEntity)

    /**
     * Actualiza la contraseña del usuario, limpiando los códigos de recuperación.
     */
    @Query("""
        UPDATE users SET passwordHash=:hash, passwordSalt=:salt,
        recoveryCode=NULL, recoveryCodeExpiresAt=NULL WHERE id=:userId
    """)
    suspend fun updatePassword(userId: Long, hash: String, salt: String)

    /**
     * Asigna un código de recuperación (por ejemplo, para "Olvidé mi contraseña").
     */
    @Query("UPDATE users SET recoveryCode=:code, recoveryCodeExpiresAt=:expiresAt WHERE id=:userId")
    suspend fun setRecovery(userId: Long, code: String, expiresAt: Long)

    /**
     * Actualiza el balance del usuario (saldo disponible en el casino).
     */
    @Query("UPDATE users SET balance = :newBalance WHERE id = :userId")
    suspend fun updateBalance(userId: Long, newBalance: Int)
}
