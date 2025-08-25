package com.example678.kiranafinder2.domain.repository



import com.example678.kiranafinder2.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<UserLocation>
    fun getLocationUpdates(): Flow<UserLocation>
    suspend fun isLocationEnabled(): Boolean
    suspend fun requestLocationPermission(): Boolean
}
