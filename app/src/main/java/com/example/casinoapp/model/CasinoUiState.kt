
package com.example.casinoapp.model

data class CasinoUiState(
    val isLoggedIn: Boolean = false,
    val playerName: String = "",
    val balance: Int = 0,
    val statusMessage: String? = null,
    val history: List<String> = emptyList()
)
