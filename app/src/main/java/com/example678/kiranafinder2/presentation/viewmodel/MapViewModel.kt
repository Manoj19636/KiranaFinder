package com.example678.kiranafinder2.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example678.kiranafinder2.domain.model.*
import com.example678.kiranafinder2.domain.repository.AuthRepository
import com.example678.kiranafinder2.domain.usecase.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getNearbyStoresUseCase: GetNearbyStoresUseCase,
    private val updateStoreStatusUseCase: UpdateStoreStatusUseCase,
    private val addNewStoreUseCase: AddNewStoreUseCase,
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val filterNearbyStoresUseCase: FilterNearbyStoresUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "MapViewModel"
    }

    init {
        // Auto-detect location on start
        getCurrentLocation()
    }

    // 🌍 LOCATION MANAGEMENT
    fun getCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLocationLoading = true,
                locationError = null
            )

            Log.d(TAG, "🗺️ Getting current location...")

            getCurrentLocationUseCase()
                .onSuccess { userLocation ->
                    Log.d(TAG, "✅ Location obtained: ${userLocation.latitude}, ${userLocation.longitude}")

                    val latLng = LatLng(userLocation.latitude, userLocation.longitude)

                    _uiState.value = _uiState.value.copy(
                        userLocation = userLocation,
                        currentLocation = latLng,
                        isLocationLoading = false,
                        locationError = null
                    )

                    // Load stores near this location
                    loadNearbyStores(userLocation.latitude, userLocation.longitude)
                }
                .onFailure { error ->
                    Log.e(TAG, "❌ Location error: ${error.message}")

                    _uiState.value = _uiState.value.copy(
                        isLocationLoading = false,
                        locationError = error.message,
                        showLocationPermissionDialog = error is SecurityException
                    )

                    // Fallback to Delhi location
                    setFallbackLocation()
                }
        }
    }

    private fun setFallbackLocation() {
        Log.d(TAG, "📍 Using fallback location: Delhi")
        val delhiLocation = LatLng(28.6139, 77.2090)
        _uiState.value = _uiState.value.copy(
            currentLocation = delhiLocation,
            isLocationLoading = false
        )
        loadNearbyStores(delhiLocation.latitude, delhiLocation.longitude)
    }

    fun setCurrentLocation(location: LatLng) {
        _uiState.value = _uiState.value.copy(currentLocation = location)
        loadNearbyStores(location.latitude, location.longitude)
    }

    fun centerOnUserLocation() {
        Log.d(TAG, "🎯 Centering on user location")
        _uiState.value.userLocation?.let { userLocation ->
            val latLng = LatLng(userLocation.latitude, userLocation.longitude)
            _uiState.value = _uiState.value.copy(currentLocation = latLng)
            Log.d(TAG, "✅ Map centered on: ${latLng.latitude}, ${latLng.longitude}")
        } ?: run {
            Log.w(TAG, "⚠️ No user location available, requesting location...")
            getCurrentLocation()
        }
    }

    // 🏪 STORE MANAGEMENT
    private fun loadNearbyStores(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            getNearbyStoresUseCase(latitude, longitude)
                .catch { error ->
                    Log.e(TAG, "❌ Error loading stores: ${error.message}")
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
                .collect { allStores ->
                    Log.d(TAG, "🏪 Loaded ${allStores.size} total stores")

                    // Filter stores within radius if user location is available
                    val nearbyStores = _uiState.value.userLocation?.let { userLocation ->
                        filterNearbyStoresUseCase(
                            stores = allStores,
                            userLocation = userLocation,
                            radiusKm = _uiState.value.radiusKm
                        )
                    } ?: allStores

                    Log.d(TAG, "📍 ${nearbyStores.size} stores within ${_uiState.value.radiusKm}km radius")

                    _uiState.value = _uiState.value.copy(
                        stores = allStores,
                        nearbyStores = nearbyStores,
                        isLoading = false
                    )
                }
        }
    }

    fun selectStore(store: Store) {
        Log.d(TAG, "🎯 Store selected: ${store.name}")
        _uiState.value = _uiState.value.copy(
            selectedStore = store,
            showStoreDialog = true
        )
    }

    fun dismissStoreDialog() {
        _uiState.value = _uiState.value.copy(
            selectedStore = null,
            showStoreDialog = false
        )
    }

    // 🔄 UPDATE STORE STATUS WITH USER STATS INCREMENT
    fun updateStoreStatus(storeId: String, status: StoreStatus, note: String = "") {
        Log.d(TAG, "🔄 Updating store status: $storeId -> $status")

        viewModelScope.launch {
            try {
                // Get current user ID
                val currentUser = authRepository.getCurrentUser()
                val userId = currentUser?.id

                if (userId != null) {
                    // Update the store
                    updateStoreStatusUseCase(storeId, status, note)

                    // 🎯 INCREMENT USER STATS
                    val reputationPoints = when {
                        note.isNotBlank() && note.length > 10 -> 10 // Extra points for helpful notes
                        else -> 5 // Standard points for status update
                    }

                    // Update user statistics in Firestore
                    authRepository.updateUserStats(
                        userId = userId,
                        contributionIncrease = 1,
                        reputationIncrease = reputationPoints
                    ).onSuccess {
                        Log.d(TAG, "✅ User stats updated: +1 contribution, +$reputationPoints reputation")
                    }.onFailure { error ->
                        Log.e(TAG, "❌ Failed to update user stats: ${error.message}")
                    }

                    // Refresh stores after update
                    _uiState.value.currentLocation?.let { location ->
                        loadNearbyStores(location.latitude, location.longitude)
                    }
                    dismissStoreDialog()

                } else {
                    Log.w(TAG, "⚠️ No current user - cannot update stats")
                    _uiState.value = _uiState.value.copy(error = "Please sign in to update stores")
                }

            } catch (error: Exception) {
                Log.e(TAG, "❌ Error updating store: ${error.message}")
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        }
    }

    // 🗺️ MAP INTERACTIONS
    fun onMapLongPress(location: LatLng) {
        Log.d(TAG, "📍 Map long pressed at: (${location.latitude}, ${location.longitude})")
        _uiState.value = _uiState.value.copy(
            newStoreLocation = location,
            showNewStoreDialog = true
        )
    }

    fun dismissNewStoreDialog() {
        _uiState.value = _uiState.value.copy(
            newStoreLocation = null,
            showNewStoreDialog = false
        )
    }

    // 🆕 ADD NEW STORE WITH USER STATS INCREMENT
    fun addNewStore(name: String, address: String) {
        val location = _uiState.value.newStoreLocation
        if (location != null && name.isNotBlank()) {
            viewModelScope.launch {
                try {
                    // Get current user ID
                    val currentUser = authRepository.getCurrentUser()
                    val userId = currentUser?.id

                    if (userId != null) {
                        Log.d(TAG, "🆕 Adding new store: $name by user: $userId")

                        val newStore = Store(
                            id = "${name.hashCode()}_${System.currentTimeMillis()}",
                            name = name.trim(),
                            latitude = location.latitude,
                            longitude = location.longitude,
                            status = StoreStatus.UNKNOWN,
                            address = address.trim(),
                            lastUpdated = System.currentTimeMillis(),
                            lastNote = "New store added by community",
                            reportedBy = userId,
                            reportedByName = currentUser.displayName,
                            reportedByPhotoUrl = currentUser.photoUrl
                        )

                        addNewStoreUseCase(newStore)

                        // 🎯 INCREMENT USER STATS FOR ADDING NEW STORE
                        authRepository.updateUserStats(
                            userId = userId,
                            contributionIncrease = 1,
                            reputationIncrease = 15 // Higher points for adding new store
                        ).onSuccess {
                            Log.d(TAG, "✅ User stats updated: +1 contribution, +15 reputation for new store")
                        }.onFailure { error ->
                            Log.e(TAG, "❌ Failed to update user stats: ${error.message}")
                        }

                        // Refresh stores
                        _uiState.value.currentLocation?.let { currentLocation ->
                            loadNearbyStores(currentLocation.latitude, currentLocation.longitude)
                        }
                        dismissNewStoreDialog()

                    } else {
                        Log.w(TAG, "⚠️ No current user - cannot add store")
                        _uiState.value = _uiState.value.copy(error = "Please sign in to add stores")
                    }

                } catch (error: Exception) {
                    Log.e(TAG, "❌ Error adding store: ${error.message}")
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            }
        }
    }

    // 🔵 RADIUS MANAGEMENT
    fun toggleRadiusCircle() {
        _uiState.value = _uiState.value.copy(
            showRadiusCircle = !_uiState.value.showRadiusCircle
        )
    }

    fun updateRadius(radiusKm: Double) {
        _uiState.value = _uiState.value.copy(radiusKm = radiusKm)

        // Re-filter stores with new radius
        _uiState.value.userLocation?.let { userLocation ->
            val nearbyStores = filterNearbyStoresUseCase(
                stores = _uiState.value.stores,
                userLocation = userLocation,
                radiusKm = radiusKm
            )

            _uiState.value = _uiState.value.copy(nearbyStores = nearbyStores)
        }
    }

    // ❌ ERROR HANDLING
    fun dismissLocationError() {
        _uiState.value = _uiState.value.copy(
            locationError = null,
            showLocationPermissionDialog = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
