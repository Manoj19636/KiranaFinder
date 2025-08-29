package com.example678.kiranafinder2.presentation.ui.map

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example678.kiranafinder2.domain.model.Store
import com.example678.kiranafinder2.domain.model.StoreStatus
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun StoreMarkers(
    stores: List<Store>,
    onMarkerClick: (Store) -> Unit
) {
    Log.d("StoreMarkers", "🎯 Rendering ${stores.size} markers")

    stores.forEachIndexed { index, store ->
        Log.d("StoreMarkers", "🎯 Marker #$index: ${store.name} - Status: ${store.status}")

        val title = store.name.ifBlank { "Unknown Store" }

        // ✅ Color mapping with logging
        val markerColor = when (store.status) {
            StoreStatus.OPEN -> {
                Log.d("StoreMarkers", "🟢 OPEN store - Setting GREEN color")
                Color(0xFF4CAF50) // Bright Green
            }
            StoreStatus.CLOSED -> {
                Log.d("StoreMarkers", "🔴 CLOSED store - Setting RED color")
                Color(0xFFE53935) // Bright Red
            }
            StoreStatus.UNKNOWN -> {
                Log.d("StoreMarkers", "🔵 UNKNOWN store - Setting CYAN color")
                Color(0xFF00BCD4) // Bright Cyan
            }
        }

        // ✅ FIXED: Use keys parameter correctly
        MarkerComposable(
            store.id, store.status, // ✅ Pass keys as individual arguments
            state = MarkerState(
                position = LatLng(store.latitude, store.longitude)
            ),
            onClick = { marker ->
                Log.d("StoreMarkers", "🎯 EXACT marker click: ${store.name}")
                onMarkerClick(store)
                true
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(70.dp)
                    .wrapContentHeight()
            ) {
                // ✅ Yellow label for store name
                Surface(
                    modifier = Modifier.padding(bottom = 4.dp),
                    color = Color.Yellow,
                    shape = RoundedCornerShape(6.dp),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // ✅ Status indicator text for debugging
                Text(
                    text = store.status.name,
                    fontSize = 6.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                // ✅ MARKER ICON with proper color application
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Store Location",
                    modifier = Modifier.size(28.dp),
                    tint = markerColor
                )

                // ✅ Debug: Show color as small dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(markerColor, androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    }
}

// ✅ UTILITY: Helper function for time formatting
fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (1000 * 60)

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        else -> "${minutes / 60}h ago"
    }
}
