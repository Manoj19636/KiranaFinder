package com.example678.kiranafinder2.presentation.ui.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Edit  // or Create
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example678.kiranafinder2.domain.model.getContributionText
import com.example678.kiranafinder2.domain.model.getReputationLevel
import com.example678.kiranafinder2.presentation.viewmodel.AuthViewModel


@Composable
fun ProfileScreen(
    onSignOut: () -> Unit = {},
    onBack: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val user = authState.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Back to Map")
            }

            TextButton(
                onClick = onSignOut,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Sign Out")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (user != null) {
            // Profile Header
            ProfileHeader(user = user)

            Spacer(modifier = Modifier.height(32.dp))

            // Stats Cards
            ProfileStats(user = user)

            Spacer(modifier = Modifier.height(24.dp))

            // Community Impact
            CommunityImpactCard(user = user)

        } else {
            // Error state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Unable to load profile. Please try signing in again.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: com.example678.kiranafinder2.domain.model.User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Photo
        AsyncImage(
            model = user.photoUrl.ifEmpty { "https://via.placeholder.com/80" },
            contentDescription = "Profile Photo",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // User Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.displayName.ifEmpty { "Anonymous User" },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = user.email,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reputation Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = user.getReputationLevel(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ProfileStats(user: com.example678.kiranafinder2.domain.model.User) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Contributions
        StatCard(
            icon = Icons.Default.Edit,
            title = "Contributions",
            value = user.totalContributions.toString(),
            subtitle = user.getContributionText(),
            modifier = Modifier.weight(1f)
        )

        // Reputation
        StatCard(
            icon = Icons.Default.Star,
            title = "Reputation",
            value = user.reputation.toString(),
            subtitle = "Community points",
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CommunityImpactCard(user: com.example678.kiranafinder2.domain.model.User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🌟 Community Impact",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            val impactText = when {
                user.totalContributions >= 50 -> "You're a community hero! Your updates help hundreds of neighbors daily."
                user.totalContributions >= 20 -> "Amazing work! You're helping build a better community platform."
                user.totalContributions >= 5 -> "Great start! Your contributions make a real difference."
                else -> "Welcome to the community! Start updating stores to help your neighbors."
            }

            Text(
                text = impactText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                lineHeight = 20.sp
            )

            if (user.totalContributions > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Thank you for making Evening Essentials Finder valuable for everyone! 🙏",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
