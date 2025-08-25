package com.example678.kiranafinder2.domain.model


data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),

    // New profile fields
    val totalContributions: Int = 0,
    val reputation: Int = 0,
    val isVerified: Boolean = false,
    val lastActiveAt: Long = System.currentTimeMillis(),

    // Location info (for next phase)
    val currentCity: String = "",
    val prefersMetricUnits: Boolean = true
)

// Helper functions
fun User.getReputationLevel(): String = when {
    reputation >= 100 -> "🏆 Community Hero"
    reputation >= 50 -> "⭐ Trusted Contributor"
    reputation >= 20 -> "👍 Active Member"
    reputation >= 5 -> "🌱 New Contributor"
    else -> "👋 Community Member"
}

fun User.getContributionText(): String = when {
    totalContributions == 0 -> "No contributions yet"
    totalContributions == 1 -> "1 store update"
    else -> "$totalContributions store updates"
}

