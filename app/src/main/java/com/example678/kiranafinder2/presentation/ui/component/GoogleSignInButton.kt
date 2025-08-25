package com.example678.kiranafinder2.presentation.ui.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example678.kiranafinder2.R
import kotlinx.coroutines.launch

@Composable
fun GoogleSignInButton(
    onTokenReceived: (String) -> Unit,
    onError: (String) -> Unit,
    isLoading: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 🎯 MODERN: Credential Manager (replaces deprecated GoogleSignInClient)
    val credentialManager = remember {
        CredentialManager.create(context)
    }

    // 🚀 Modern Google Sign-In Button
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(enabled = !isLoading) {
                if (!isLoading) {
                    Log.d("GoogleSignInButton", "🚀 Sign-In button clicked")

                    coroutineScope.launch {
                        try {
                            // Step 1: Configure Google ID option
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false) // Show all accounts
                                .setServerClientId(context.getString(R.string.default_web_client_id))
                                .setAutoSelectEnabled(false) // Force account picker
                                .build()

                            // Step 2: Create credential request
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()

                            Log.d("GoogleSignInButton", "📋 Launching Credential Manager")

                            // Step 3: Get credential (shows account picker)
                            val result = credentialManager.getCredential(
                                request = request,
                                context = context
                            )

                            // Step 4: Extract Google ID token
                            val credential = result.credential
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken

                            if (idToken != null) {
                                Log.d("GoogleSignInButton", "✅ ID Token received successfully")
                                onTokenReceived(idToken)
                            } else {
                                Log.e("GoogleSignInButton", "❌ ID Token is null")
                                onError("Failed to get ID token")
                            }

                        } catch (e: GetCredentialException) {
                            Log.e("GoogleSignInButton", "❌ Credential Manager error: ${e.type}")

                            when (e.type) {
                                "android.credentials.GetCredentialException.TYPE_USER_CANCELED" -> {
                                    onError("Sign-in was cancelled")
                                }
                                "android.credentials.GetCredentialException.TYPE_NO_CREDENTIAL" -> {
                                    onError("No Google accounts found")
                                }
                                else -> {
                                    onError("Sign-in failed: ${e.message}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("GoogleSignInButton", "❌ General error: ${e.message}")
                            onError("Sign-in error: ${e.message}")
                        }
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLoading) Color.Gray.copy(alpha = 0.3f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Signing in...",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google Sign-In",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continue with Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}
