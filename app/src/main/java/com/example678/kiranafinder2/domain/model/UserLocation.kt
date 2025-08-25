package com.example678.kiranafinder2.domain.model


data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromGPS: Boolean = true
)

// Distance calculation helper
fun UserLocation.distanceTo(store: Store): Double {
    return calculateDistance(
        lat1 = this.latitude,
        lon1 = this.longitude,
        lat2 = store.latitude,
        lon2 = store.longitude
    )
}

// Haversine formula for distance calculation
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0 // Earth's radius in kilometers

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)

    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))

    return R * c
}
