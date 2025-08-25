package com.example678.kiranafinder2.presentation.ui.component


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    content: @Composable () -> Unit
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    when {
        locationPermissionsState.allPermissionsGranted -> {
            LaunchedEffect(Unit) {
                onPermissionGranted()
            }
            content()
        }
        locationPermissionsState.shouldShowRationale -> {
            LocationPermissionRationale(
                onRequestPermission = { locationPermissionsState.launchMultiplePermissionRequest() },
                onDeny = onPermissionDenied
            )
        }
        else -> {
            LaunchedEffect(Unit) {
                locationPermissionsState.launchMultiplePermissionRequest()
            }
            LocationPermissionInitial(
                onRequestPermission = { locationPermissionsState.launchMultiplePermissionRequest() },
                onDeny = onPermissionDenied
            )
        }
    }
}

@Composable
private fun LocationPermissionRationale(
    onRequestPermission: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDeny,
        title = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Location Access Needed")
            }
        },
        text = {
            Column {
                Text(
                    text = "Evening Essentials Finder needs location access to:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                LocationBenefit("📍 Show stores near you")
                LocationBenefit("🎯 Display 5km coverage area")
                LocationBenefit("🚶 Calculate walking distances")
                LocationBenefit("🗺️ Provide better recommendations")

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your location is only used locally and never shared.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text("Not Now")
            }
        }
    )
}

@Composable
private fun LocationPermissionInitial(
    onRequestPermission: () -> Unit,
    onDeny: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🗺️ Get Personalized Store Recommendations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Allow location access to find kirana stores within 5km of your location!",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDeny,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Skip")
                }

                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Allow Location")
                }
            }
        }
    }
}

@Composable
private fun LocationBenefit(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
