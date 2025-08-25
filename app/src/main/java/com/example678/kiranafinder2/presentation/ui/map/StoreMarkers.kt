package com.example678.kiranafinder2.presentation.ui.map

import android.util.Log
import androidx.compose.runtime.Composable
import com.example678.kiranafinder2.domain.model.Store
import com.example678.kiranafinder2.domain.model.StoreStatus
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun StoreMarkers(
    stores: List<Store>,
    onMarkerClick: (Store) -> Unit
) {
    Log.d("StoreMarkers", "🎯 Rendering ${stores.size} markers")

    stores.forEachIndexed { index, store ->
        Log.d("StoreMarkers", "🎯 Marker #$index: ${store.name} (ID: ${store.id}) at (${store.latitude}, ${store.longitude})")

        val markerIcon = when (store.status) {
            StoreStatus.OPEN -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            StoreStatus.CLOSED -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            StoreStatus.UNKNOWN -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        }

        val title = store.name.ifBlank { "Unknown Store" }

        Marker(
            state = MarkerState(position = LatLng(store.latitude, store.longitude)),
            title = title,
            snippet = buildString {
                append("Status: ${store.status.name}")
                if (store.lastNote.isNotBlank()) {
                    append("\n💬 ${store.lastNote}")
                }
                append("\n⏰ Updated ${formatTimeAgo(store.lastUpdated)}")
            },
            icon = markerIcon,
            onClick = {
                Log.d("StoreMarkers", "🔥 MARKER CLICKED: ${store.name} (ID: ${store.id}) at (${store.latitude}, ${store.longitude})")
                onMarkerClick(store)
                true
            }
        )
    }
}

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
