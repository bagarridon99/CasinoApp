package com.example.casinoapp.data.entity

import androidx.room.*
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val email: String,
    val passwordHash: String,
    val passwordSalt: String,
    val recoveryCode: String? = null,
    val recoveryCodeExpiresAt: Long? = null
)