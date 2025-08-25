package com.example678.kiranafinder2.data.repository

import android.content.Context
import android.util.Log
import com.example678.kiranafinder2.R
import com.example678.kiranafinder2.domain.model.User
import com.example678.kiranafinder2.domain.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthRepository {

    private val auth: FirebaseAuth = Firebase.auth.apply {
        // Ensure proper auth persistence
        useAppLanguage()
    }

    private val firestore: FirebaseFirestore = Firebase.firestore
    private val usersCollection = firestore.collection("users")

    companion object {
        private const val TAG = "AuthRepository"
    }

    init {
        Log.d(TAG, "🚀 AuthRepository initialized - Current user: ${auth.currentUser?.email}")
    }

    // 🔐 Auth state flow
    override val authState: Flow<Boolean> = callbackFlow {
        Log.d(TAG, "🎬 STEP 1: Setting up auth state listener")

        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val isAuthenticated = firebaseAuth.currentUser != null
            val userEmail = firebaseAuth.currentUser?.email ?: "null"
            Log.d(TAG, "🔐 STEP 2: Auth state changed - isAuthenticated: $isAuthenticated, email: $userEmail")
            trySend(isAuthenticated)
        }

        auth.addAuthStateListener(listener)
        Log.d(TAG, "✅ STEP 3: Auth listener attached")

        awaitClose {
            Log.d(TAG, "🔇 STEP FINAL: Removing auth state listener")
            auth.removeAuthStateListener(listener)
        }
    }

    // 👤 Current user flow
    override val currentUser: Flow<User?> = callbackFlow {
        val currentFirebaseUser = auth.currentUser
        Log.d(TAG, "👤 STEP 4: Checking current Firebase user: ${currentFirebaseUser?.email ?: "null"}")

        if (currentFirebaseUser == null) {
            Log.d(TAG, "❌ STEP 5: No Firebase user - sending null")
            trySend(null)
            close()
        } else {
            Log.d(TAG, "🎧 STEP 6: Setting up Firestore listener for user: ${currentFirebaseUser.uid}")

            val listenerRegistration: ListenerRegistration = usersCollection
                .document(currentFirebaseUser.uid)
                .addSnapshotListener { snapshot, error ->
                    Log.d(TAG, "📡 STEP 7: Firestore listener triggered")

                    if (error != null) {
                        Log.e(TAG, "❌ STEP 8: Firestore error: ${error.message}")
                        trySend(null)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        Log.d(TAG, "📄 STEP 9: Snapshot exists: ${snapshot.exists()}")

                        if (snapshot.exists()) {
                            val user = snapshot.toObject(User::class.java)
                            Log.d(TAG, "✅ STEP 10: User object created: ${user?.displayName ?: "null"}, email: ${user?.email ?: "null"}")
                            trySend(user)
                        } else {
                            Log.w(TAG, "⚠️ STEP 11: Document doesn't exist - sending null")
                            trySend(null)
                        }
                    } else {
                        Log.e(TAG, "❌ STEP 12: Snapshot is null")
                        trySend(null)
                    }
                }

            awaitClose {
                Log.d(TAG, "🔇 STEP FINAL: Removing Firestore listener")
                listenerRegistration.remove()
            }
        }
    }
    override suspend fun signInWithGoogle(): Result<User> {
        return try {
            Log.d(TAG, "🚀 Starting Google Sign-In (Fast Mode)")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            val silentSignInTask = googleSignInClient.silentSignIn()

            val account = if (silentSignInTask.isSuccessful) {
                Log.d(TAG, "✅ Silent sign-in successful")
                silentSignInTask.result
            } else {
                Log.d(TAG, "❌ Silent sign-in failed")
                return Result.failure(Exception("Manual sign-in required"))
            }

            val idToken = account?.idToken ?: return Result.failure(Exception("No ID token"))
            Log.d(TAG, "✅ ID token: ${account.email}")

            // Firebase auth
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Firebase auth failed"))

            Log.d(TAG, "✅ Firebase auth successful: ${firebaseUser.email}")

            // 🎯 STEP 4 FIX: Create user object WITHOUT waiting for Firestore
            val user = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                createdAt = System.currentTimeMillis(),
                totalContributions = 0,
                reputation = 0,
                isVerified = false,
                lastActiveAt = System.currentTimeMillis()
            )

            // 🎯 CREATE DOCUMENT IN BACKGROUND - DON'T WAIT!
            CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
                try {
                    usersCollection.document(user.id).set(user, SetOptions.merge()).await()
                    Log.d(TAG, "✅ Background document creation completed")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Background document creation failed: ${e.message}")
                }
            }

            // 🎯 RETURN IMMEDIATELY
            Log.d(TAG, "🚀 RETURNING USER IMMEDIATELY")
            Result.success(user)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Sign-in error: ${e.message}")
            Result.failure(e)
        }
    }


    // 🚀 OPTIMIZED Google Sign-In
//    override suspend fun signInWithGoogle(): Result<User> {
//        return try {
//            Log.d(TAG, "🚀 Starting Google Sign-In (Optimized)")
//
//            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(context.getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build()
//
//            val googleSignInClient = GoogleSignIn.getClient(context, gso)
//            val silentSignInTask = googleSignInClient.silentSignIn()
//
//            val account = if (silentSignInTask.isSuccessful) {
//                Log.d(TAG, "✅ Silent sign-in successful")
//                silentSignInTask.result
//            } else {
//                Log.d(TAG, "❌ Silent sign-in failed, need manual sign-in")
//                return Result.failure(Exception("Manual sign-in required - use button"))
//            }
//
//            val idToken = account?.idToken
//            if (idToken == null) {
//                Log.e(TAG, "❌ Google ID token is null")
//                return Result.failure(Exception("Google ID token is null"))
//            }
//
//            Log.d(TAG, "✅ Google ID token obtained: ${account.email ?: "unknown"}")
//
//            // Firebase authentication
//            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
//            Log.d(TAG, "🔑 Firebase credential created, signing into Firebase...")
//
//            val authResult = auth.signInWithCredential(firebaseCredential).await()
//            val firebaseUser = authResult.user
//
//            if (firebaseUser == null) {
//                Log.e(TAG, "❌ Firebase user is null after authentication")
//                return Result.failure(Exception("Firebase authentication failed"))
//            }
//
//            Log.d(TAG, "✅ Firebase authentication successful: ${firebaseUser.email ?: "unknown"}")
//
//            // Create user object
//            val user = User(
//                id = firebaseUser.uid,
//                email = firebaseUser.email ?: "",
//                displayName = firebaseUser.displayName ?: "",
//                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
//                createdAt = System.currentTimeMillis(),
//                totalContributions = 0,
//                reputation = 0,
//                isVerified = false,
//                lastActiveAt = System.currentTimeMillis()
//            )
//
//            // 🎯 OPTIMIZATION: Create user document asynchronously to avoid blocking
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    usersCollection.document(user.id)
//                        .set(user, SetOptions.merge())
//                        .await()
//                    Log.d(TAG, "✅ User document created successfully - AUTHENTICATION COMPLETE!")
//                } catch (e: Exception) {
//                    Log.e(TAG, "⚠️ User document creation failed (non-critical): ${e.message}")
//                }
//            }
//
//            // Return immediately after Firebase auth succeeds
//            Log.d(TAG, "🚀 Returning success immediately - document creation in background")
//            Result.success(user)
//
//        } catch (e: Exception) {
//            Log.e(TAG, "❌ Sign-in error: ${e.message}")
//            Result.failure(e)
//        }
//    }

    // 👋 Sign out
    override suspend fun signOut() {
        try {
            Log.d(TAG, "👋 Signing out user from Firebase")
            auth.signOut()

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut().await()

            Log.d(TAG, "✅ User signed out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Sign out error: ${e.message}")
        }
    }

    // 👤 Get current user
    override suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val document = usersCollection.document(firebaseUser.uid).get().await()
                document.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting current user: ${e.message}")
            null
        }
    }

    // 📈 User statistics methods
    override suspend fun incrementUserContributions(userId: String, amount: Int): Result<Unit> {
        return try {
            usersCollection.document(userId).update(
                mapOf(
                    "totalContributions" to FieldValue.increment(amount.toLong()),
                    "lastActiveAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun incrementUserReputation(userId: String, points: Int): Result<Unit> {
        return try {
            usersCollection.document(userId).update(
                mapOf(
                    "reputation" to FieldValue.increment(points.toLong()),
                    "lastActiveAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserStats(userId: String, contributionIncrease: Int, reputationIncrease: Int): Result<Unit> {
        return try {
            usersCollection.document(userId).update(
                mapOf(
                    "totalContributions" to FieldValue.increment(contributionIncrease.toLong()),
                    "reputation" to FieldValue.increment(reputationIncrease.toLong()),
                    "lastActiveAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
