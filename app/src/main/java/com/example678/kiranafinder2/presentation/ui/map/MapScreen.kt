package com.example678.kiranafinder2.presentation.ui.map

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example678.kiranafinder2.presentation.ui.component.AddNewStoreDialog
import com.example678.kiranafinder2.presentation.ui.component.StoreDialog
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example678.kiranafinder2.presentation.ui.component.LocationPermissionHandler
import com.example678.kiranafinder2.presentation.ui.component.StoreDialog
import com.example678.kiranafinder2.presentation.viewmodel.MapViewModel
import com.google.android.gms.maps.CameraUpdateFactory

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Camera position state - starts with fallback location
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(28.6139, 77.2090), // Default: Delhi
            15f
        )
    }

    // 🎯 KEY FIX: Animate camera when user location is detected
    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let { userLocation ->
            val userLatLng = LatLng(userLocation.latitude, userLocation.longitude)
            Log.d("MapScreen", "🎥 Animating camera to user location: $userLatLng")

            // ✅ CORRECT: Use animate() with CameraPosition directly
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(userLatLng, 16f)
                ),
                durationMs = 1500 // Smooth 1.5 second animation
            )
        }
    }

    // 🌍 GLOBAL LOCATION DETECTION: Works for users anywhere in the world
    LocationPermissionHandler(
        onPermissionGranted = {
            Log.d("MapScreen", "📍 Location permission granted - detecting user location...")
            viewModel.getCurrentLocation()
        },
        onPermissionDenied = {
            Log.w("MapScreen", "❌ Location permission denied")
            viewModel.dismissLocationError()
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Loading State
            if (uiState.isLoading || uiState.isLocationLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when {
                                uiState.isLocationLoading -> "🌍 Finding your location..."
                                else -> "📍 Loading nearby stores..."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Google Map with proper camera control
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapLongClick = { latLng ->
                    viewModel.onMapLongPress(latLng)
                }
            ) {
                // Store Markers (filtered by radius)
                StoreMarkers(
                    stores = uiState.nearbyStores,
                    onMarkerClick = viewModel::selectStore
                )

                // 📍 User Location Marker (works globally)
                uiState.userLocation?.let { userLocation ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                userLocation.latitude,
                                userLocation.longitude
                            )
                        ),
                        title = "Your Location",
                        snippet = "📍 Accuracy: ${userLocation.accuracy.toInt()}m",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_BLUE
                        )
                    )
                }

                // 🔵 5km Radius Circle (centered on user location)
                if (uiState.showRadiusCircle && uiState.userLocation != null) {
                    Circle(
                        center = LatLng(
                            uiState.userLocation!!.latitude,
                            uiState.userLocation!!.longitude
                        ),
                        radius = uiState.radiusKm * 1000, // Convert km to meters
                        strokeColor = Color(0xFF2196F3), // Blue stroke
                        strokeWidth = 3f,
                        fillColor = Color(0x1A2196F3), // Transparent blue fill
                        clickable = false
                    )
                }

                // 🟡 New Store Location Marker
                if (uiState.showNewStoreDialog) {
                    uiState.newStoreLocation?.let { newLocation ->
                        Marker(
                            state = MarkerState(position = newLocation),
                            title = "New Store Location",
                            snippet = "Tap to add store details",
                            icon = BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_YELLOW
                            )
                        )
                    }
                }
            }

            // 🎯 Floating Action Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Profile Button
                FloatingActionButton(
                    onClick = onProfileClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                // 📍 My Location Button (works globally)
                FloatingActionButton(
                    onClick = {
                        Log.d("MapScreen", "🔥 My Location button clicked!")
                        Toast.makeText(context, "🌍 Finding your location...", Toast.LENGTH_SHORT).show()
                        viewModel.getCurrentLocation() // Re-detect location and animate camera
                    },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "My Location",
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }

                // 🔵 Toggle Radius Circle Button
                FloatingActionButton(
                    onClick = {
                        Log.d("MapScreen", "🔥 Toggle radius button clicked!")
                        viewModel.toggleRadiusCircle()
                    },
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(
                        imageVector = if (uiState.showRadiusCircle)
                            Icons.Default.Check
                        else
                            Icons.Default.Add,
                        contentDescription = "Toggle Radius",
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }

            // 📊 Location Info Card
            if (uiState.userLocation != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🌍 Your Location:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${uiState.userLocation!!.latitude.format(4)}, ${uiState.userLocation!!.longitude.format(4)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.nearbyStores.isNotEmpty()) {
                            Text(
                                text = "🏪 ${uiState.nearbyStores.size} stores within ${uiState.radiusKm}km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "🎯 GPS accuracy: ${uiState.userLocation!!.accuracy.toInt()}m",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ⚠️ No Location Available Card
            if (uiState.userLocation == null && !uiState.isLocationLoading && uiState.locationError != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "❌ Location Error",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = uiState.locationError ?: "Unable to detect location",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.getCurrentLocation() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Try Again", color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            }
        }

        // Store Dialog
        if (uiState.showStoreDialog && uiState.selectedStore != null) {
            StoreDialog(
                store = uiState.selectedStore!!,
                onDismiss = viewModel::dismissStoreDialog,
                onUpdateStatus = { status, note ->
                    viewModel.updateStoreStatus(uiState.selectedStore!!.id, status, note)
                }
            )
        }

        // Add New Store Dialog
        if (uiState.showNewStoreDialog) {
            AddNewStoreDialog(
                location = uiState.newStoreLocation,
                onDismiss = viewModel::dismissNewStoreDialog,
                onAddStore = { name, address ->
                    viewModel.addNewStore(name, address)
                }
            )
        }

        // Error Handling with Auto-dismiss
        if (uiState.locationError != null) {
            LaunchedEffect(uiState.locationError) {
                // Auto-dismiss error after 5 seconds
                kotlinx.coroutines.delay(5000)
                viewModel.dismissLocationError()
            }
        }
    }
}

// Extension function for formatting coordinates
fun Double.format(decimals: Int) = "%.${decimals}f".format(this)
