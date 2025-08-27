package com.example678.kiranafinder2

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example678.kiranafinder2.presentation.ui.auth.SignInScreen
import com.example678.kiranafinder2.presentation.ui.map.MapScreen
import com.example678.kiranafinder2.presentation.ui.profile.ProfileScreen
import com.example678.kiranafinder2.presentation.ui.theme.KiranaFinder2Theme
import com.example678.kiranafinder2.presentation.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("MainActivity", "Location permissions result: $permissions")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request location permissions for map functionality
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        setContent {
            KiranaFinder2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EveningEssentialsApp()
                }
            }
        }
    }
}

@Composable
fun EveningEssentialsApp(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val navController = rememberNavController()

    // 🎯 ENHANCED: Comprehensive state logging
    Log.d("EveningEssentialsApp", "🎨 UI State: auth=${authState.isAuthenticated}, loading=${authState.isLoading}, user=${authState.user?.displayName}")

    // 🎯 STEP 3: Immediate check on startup
    LaunchedEffect(Unit) {
        delay(200) // Small delay for Firebase to be ready
        Log.d("EveningEssentialsApp", "🔍 Startup check: auth=${authState.isAuthenticated}")

        if (authState.isAuthenticated && authState.user != null) {
            Log.d("EveningEssentialsApp", "🚀 User already authenticated - navigating immediately")
            navController.navigate("map") {
                popUpTo("signin") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // 🎯 SIMPLIFIED: Single navigation trigger
    LaunchedEffect(authState) {
        Log.d("EveningEssentialsApp", "🔄 State changed: auth=${authState.isAuthenticated}, user=${authState.user?.displayName}, loading=${authState.isLoading}")

        if (authState.isAuthenticated && authState.user != null && !authState.isLoading) {
            Log.d("EveningEssentialsApp", "🚀 NAVIGATING NOW!")
            try {
                navController.navigate("map") {
                    popUpTo("signin") { inclusive = true }
                    launchSingleTop = true
                }
                Log.d("EveningEssentialsApp", "✅ Navigation completed")
            } catch (e: Exception) {
                Log.e("EveningEssentialsApp", "❌ Navigation error: ${e.message}")
            }
        }
    }


    NavHost(
        navController = navController,
        startDestination = "signin"
    ) {
        composable("signin") {
            Log.d("EveningEssentialsApp", "📱 COMPOSABLE: Rendering SignIn screen")

            if (authState.isLoading) {
                // Loading state with better UI
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Authenticating with Google...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This may take a moment",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Sign-in form
                SignInScreen(
                    onGoogleSignInClick = {
                        Log.d("EveningEssentialsApp", "🎯 Sign-in button clicked")
                        authViewModel.signInWithGoogle()
                    }
                )
            }
        }

        composable("map") {
            Log.d("EveningEssentialsApp", "🗺️ COMPOSABLE: Rendering Map screen - SUCCESS!")
            MapScreen(
                onProfileClick = {
                    Log.d("EveningEssentialsApp", "👤 Navigate to profile")
                    navController.navigate("profile")
                }
            )
        }

        composable("profile") {
            Log.d("EveningEssentialsApp", "👤 COMPOSABLE: Rendering Profile screen")
            ProfileScreen(
                onSignOut = {
                    Log.d("EveningEssentialsApp", "👋 Sign out requested")
                    authViewModel.signOut()
                    navController.navigate("signin") {
                        popUpTo("map") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = {
                    Log.d("EveningEssentialsApp", "🔙 Back to map")
                    navController.popBackStack()
                }
            )
        }
    }

    // Error handling
    authState.error?.let { error ->
        LaunchedEffect(error) {
            Log.e("EveningEssentialsApp", "❌ Auth error: $error")
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🚨 Authentication Error",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    TextButton(onClick = { authViewModel.clearError() }) {
                        Text("Dismiss")
                    }
                    Button(onClick = {
                        authViewModel.clearError()
                        authViewModel.signInWithGoogle()
                    }) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}
