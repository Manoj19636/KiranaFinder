package com.example678.kiranafinder2.presentation.ui.profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example678.kiranafinder2.domain.model.*
import com.example678.kiranafinder2.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit = {},
    onBack: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6200EE).copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            // 🎯 COMPACT HEADER WITH NAVIGATION
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = onSignOut,
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Logout, "Sign Out")
                    }
                }
            }

            if (user != null) {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    // 🎨 STUNNING PROFILE HEADER
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(16.dp, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primaryContainer.copy(0.3f),
                                            MaterialTheme.colorScheme.surface
                                        ),
                                        radius = 800f
                                    )
                                )
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Profile Photo with Glow
                            Box {
                                AsyncImage(
                                    model = user.photoUrl.ifEmpty { "https://via.placeholder.com/100" },
                                    contentDescription = "Profile",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .shadow(12.dp, CircleShape)
                                        .clip(CircleShape)
                                        .border(4.dp, Color.White, CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                // Verification Badge
                                if (user.isVerified) {
                                    Surface(
                                        modifier = Modifier.align(Alignment.BottomEnd),
                                        shape = CircleShape,
                                        color = Color(0xFF4CAF50)
                                    ) {
                                        Icon(
                                            Icons.Default.Verified,
                                            null,
                                            modifier = Modifier.size(24.dp).padding(4.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // User Info
                            Text(
                                text = user.displayName.ifEmpty { "Anonymous User" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = user.email,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Reputation Badge
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF4CAF50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        user.getReputationLevel(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🔥 COMPACT STATS ROW
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Contributions Card
                        Card(
                            modifier = Modifier.weight(1f).height(120.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(0.1f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(24.dp), tint = Color(0xFF2196F3))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    user.totalContributions.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2196F3)
                                )
                                Text("Contributions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Reputation Card
                        Card(
                            modifier = Modifier.weight(1f).height(120.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(0.1f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Star, null, modifier = Modifier.size(24.dp), tint = Color(0xFFFF9800))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    user.reputation.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                                Text("Reputation", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🌟 COMMUNITY IMPACT (Compact)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50).copy(0.1f),
                                    Color(0xFF8BC34A).copy(0.1f)
                                )
                            ).let { MaterialTheme.colorScheme.primaryContainer }
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4CAF50).copy(0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Groups,
                                    null,
                                    modifier = Modifier.size(24.dp).padding(12.dp),
                                    tint = Color(0xFF4CAF50)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Community Impact 🌟",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    when {
                                        user.totalContributions >= 50 -> "Community Hero! 🏆"
                                        user.totalContributions >= 20 -> "Amazing Contributor! 🚀"
                                        user.totalContributions >= 5 -> "Great Helper! 👏"
                                        else -> "Welcome! Start contributing 🎯"
                                    },
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            } else {
                // Error State (Compact)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Unable to load profile. Please try signing in again.", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }
}
