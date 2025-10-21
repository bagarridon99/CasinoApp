package com.example.casinoapp.data.dao

import androidx.room.*
import com.example.casinoapp.data.entity.UserEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    // <--- FUNCIÓN AÑADIDA ---
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?
    // <--- FIN FUNCIÓN AÑADIDA ---

    @Update
    suspend fun update(user: UserEntity)

    @Query("""
        UPDATE users SET passwordHash=:hash, passwordSalt=:salt,
        recoveryCode=NULL, recoveryCodeExpiresAt=NULL WHERE id=:userId
    """)
    suspend fun updatePassword(userId: Long, hash: String, salt: String)

    @Query("UPDATE users SET recoveryCode=:code, recoveryCodeExpiresAt=:expiresAt WHERE id=:userId")
    suspend fun setRecovery(userId: Long, code: String, expiresAt: Long)

    @Query("UPDATE users SET balance = :newBalance WHERE id = :userId")
    suspend fun updateBalance(userId: Long, newBalance: Int)
}
