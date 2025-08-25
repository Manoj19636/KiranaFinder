package com.example678.kiranafinder2.domain.model



import com.google.android.gms.maps.model.LatLng


data class MapUiState(
    val currentLocation: LatLng? = null,
    val stores: List<Store> = emptyList(),
    val isLoading: Boolean = true,
    val selectedStore: Store? = null,
    val showStoreDialog: Boolean = false,
    val error: String? = null,

    // New store registration fields
    val newStoreLocation: LatLng? = null,
    val showNewStoreDialog: Boolean = false,

    // Location features
    val userLocation: UserLocation? = null,
    val isLocationLoading: Boolean = false,
    val locationError: String? = null,
    val showLocationPermissionDialog: Boolean = false,
    val radiusKm: Double = 5.0,
    val nearbyStores: List<Store> = emptyList(),
    val showRadiusCircle: Boolean = true
)

