package com.example678.kiranafinder2.domain.model



data class AuthState(
    val isAuthenticated: Boolean = false,
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
