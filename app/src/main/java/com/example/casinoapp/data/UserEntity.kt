package com.example.casinoapp.data.entity

import androidx.room.*

/**
 * Entidad que representa la tabla "users" en la base de datos Room.
 * Cada propiedad es una columna.
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),      // Evita duplicar emails
        Index(value = ["username"], unique = true)    // Evita duplicar usernames
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L, // ID autoincremental.
    val username: String,                               // Nombre visible del usuario.
    val email: String,                                  // Email único.
    val passwordHash: String,                           // Hash de la contraseña.
    val passwordSalt: String,                           // Salt utilizado para el hash.
    val recoveryCode: String? = null,                   // Código temporal para recuperación.
    val recoveryCodeExpiresAt: Long? = null,            // Fecha de expiración del código.
    val balance: Int                                    // Saldo disponible del usuario.
)
