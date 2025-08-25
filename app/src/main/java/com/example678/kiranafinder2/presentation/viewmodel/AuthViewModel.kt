package com.example678.kiranafinder2.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example678.kiranafinder2.domain.model.AuthState
import com.example678.kiranafinder2.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    companion object {
        private const val TAG = "AuthViewModel"
    }

    init {
        Log.d(TAG, "🎬 AuthViewModel initialized")
        checkExistingAuthentication()
        observeAuthState()
    }

    // 🎯 NEW: Check for existing authentication on startup
    private fun checkExistingAuthentication() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔍 Checking for existing authentication...")
                val currentUser = authRepository.getCurrentUser()

                if (currentUser != null) {
                    Log.d(TAG, "✅ Found existing user: ${currentUser.displayName}")
                    _authState.value = AuthState(
                        isAuthenticated = true,
                        user = currentUser,
                        isLoading = false,
                        error = null
                    )
                } else {
                    Log.d(TAG, "❌ No existing user found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error checking existing auth: ${e.message}")
            }
        }
    }

    // 🔄 ENHANCED: Observe auth state with proper error handling
    private fun observeAuthState() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🎧 Starting combined auth state observation")

                combine(
                    authRepository.authState,
                    authRepository.currentUser
                ) { isAuthenticated, user ->
                    Log.d(TAG, "🔄 COMBINE: auth=$isAuthenticated, user=${user?.displayName}")

                    AuthState(
                        isAuthenticated = isAuthenticated && user != null,
                        user = user,
                        isLoading = false,
                        error = null
                    )
                }.collect { newState ->
                    Log.d(TAG, "✅ UPDATING UI STATE: authenticated=${newState.isAuthenticated}")
                    _authState.value = newState
                }

            } catch (e: CancellationException) {
                // 🎯 FIX: Handle cancellation gracefully
                Log.w(TAG, "⚠️ Auth state observation cancelled - this is normal during navigation")
                // Don't rethrow cancellation exceptions
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in auth state observation: ${e.message}")
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Authentication error: ${e.message}"
                )
            }
        }
    }

    // 🚀 Sign in with Google
    fun signInWithGoogle() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🚀 STEP X: Starting Google Sign-In")

                _authState.value = _authState.value.copy(
                    isLoading = true,
                    error = null
                )

                authRepository.signInWithGoogle()
                    .onSuccess { user ->
                        Log.d(TAG, "✅ Repository success: ${user.displayName}")

                        // 🎯 CRITICAL FIX: Force immediate state update
                        _authState.value = AuthState(
                            isAuthenticated = true,
                            user = user,
                            isLoading = false,
                            error = null
                        )
                        Log.d(TAG, "🎯 FORCED state update: authenticated=true")
                    }
                    .onFailure { error ->
                        Log.e(TAG, "❌ Sign-in failed: ${error.message}")
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            error = "Sign-in failed: ${error.message}"
                        )
                    }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception: ${e.message}")
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Sign-in error: ${e.message}"
                )
            }
        }
    }


    // 👋 Sign out
    fun signOut() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "👋 Signing out current user")
                _authState.value = _authState.value.copy(isLoading = true)

                authRepository.signOut()

                _authState.value = AuthState() // Reset to default state
                Log.d(TAG, "✅ Sign-out completed")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during sign-out: ${e.message}")
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Sign-out failed: ${e.message}"
                )
            }
        }
    }

    // 🧹 Clear error
    fun clearError() {
        Log.d(TAG, "🧹 Clearing error state")
        _authState.value = _authState.value.copy(error = null)
    }

    // 🏆 Update user stats
    fun updateUserStats(contributionIncrease: Int = 1, reputationIncrease: Int = 15) {
        viewModelScope.launch {
            try {
                val currentUser = _authState.value.user
                if (currentUser != null) {
                    Log.d(TAG, "🏆 Updating stats for user: ${currentUser.displayName}")

                    authRepository.updateUserStats(
                        userId = currentUser.id,
                        contributionIncrease = contributionIncrease,
                        reputationIncrease = reputationIncrease
                    ).onFailure { error ->
                        Log.e(TAG, "❌ Failed to update user stats: ${error.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error updating user stats: ${e.message}")
            }
        }
    }
}
