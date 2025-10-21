package com.example.casinoapp.data.entity

import androidx.room.*
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true) // <--- AÑADIDO PARA EL USERNAME
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val username: String, // <--- AÑADIDO
    val email: String,
    val passwordHash: String,
    val passwordSalt: String,
    val recoveryCode: String? = null,
    val recoveryCodeExpiresAt: Long? = null,
    val balance: Int
)