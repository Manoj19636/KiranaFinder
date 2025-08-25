package com.example678.kiranafinder2.data.repository


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example678.kiranafinder2.domain.model.UserLocation
import com.example678.kiranafinder2.domain.repository.LocationRepository
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        private const val TAG = "LocationRepository"
    }

    override suspend fun getCurrentLocation(): Result<UserLocation> {
        return try {
            if (!hasLocationPermission()) {
                return Result.failure(SecurityException("Location permission not granted"))
            }

            if (!isLocationEnabled()) {
                return Result.failure(Exception("Location services are disabled"))
            }

            Log.d(TAG, "📍 Getting current location...")

            val location = suspendCancellableCoroutine<android.location.Location> { continuation ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        Log.d(TAG, "✅ Location received: ${location.latitude}, ${location.longitude}")
                        continuation.resumeWith(Result.success(location))
                    } else {
                        Log.e(TAG, "❌ Location is null")
                        continuation.resumeWithException(Exception("Location is null"))
                    }
                }.addOnFailureListener { exception ->
                    Log.e(TAG, "❌ Failed to get location: ${exception.message}")
                    continuation.resumeWithException(exception)
                }
            }

            val userLocation = UserLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                timestamp = location.time,
                isFromGPS = location.provider == LocationManager.GPS_PROVIDER
            )

            Result.success(userLocation)

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ Security exception: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Location error: ${e.message}")
            Result.failure(e)
        }
    }

    override fun getLocationUpdates(): Flow<UserLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10000L // 10 seconds
        ).apply {
            setMinUpdateDistanceMeters(50f) // Update every 50 meters
            setMaxUpdateDelayMillis(30000L) // Max 30 seconds delay
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val userLocation = UserLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time,
                        isFromGPS = location.provider == LocationManager.GPS_PROVIDER
                    )

                    Log.d(TAG, "📍 Location update: ${location.latitude}, ${location.longitude}")
                    trySend(userLocation)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                Log.d(TAG, "📍 Location availability: ${availability.isLocationAvailable}")
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override suspend fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override suspend fun requestLocationPermission(): Boolean {
        return hasLocationPermission()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}
