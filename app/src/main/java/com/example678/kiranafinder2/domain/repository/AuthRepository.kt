package com.example678.kiranafinder2.domain.repository

import com.example678.kiranafinder2.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Authentication state flows
    val authState: Flow<Boolean>
    val currentUser: Flow<User?>

    // Authentication methods
    suspend fun signInWithGoogle(): Result<User>
    suspend fun signOut()
    suspend fun getCurrentUser(): User?

    // User statistics methods
    suspend fun incrementUserContributions(userId: String, amount: Int): Result<Unit>
    suspend fun incrementUserReputation(userId: String, points: Int): Result<Unit>
    suspend fun updateUserStats(userId: String, contributionIncrease: Int, reputationIncrease: Int): Result<Unit>
}
