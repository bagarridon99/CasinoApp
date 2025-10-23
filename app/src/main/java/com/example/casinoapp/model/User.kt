// model/User.kt
package com.example.casinoapp.model

/**
 * Modelo simple de usuario para capa de UI/dominio.
 * Nota: Esto no expone hash/salt (eso queda en la capa data/entidad Room).
 * Útil para forms de login/registro en la interfaz.
 */
data class User(
    val username: String,
    val password: String
)
