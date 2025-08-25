package com.example678.kiranafinder2.domain.usecase


import com.example678.kiranafinder2.domain.model.Store
import com.example678.kiranafinder2.domain.model.UserLocation
import com.example678.kiranafinder2.domain.model.distanceTo
import javax.inject.Inject

class FilterNearbyStoresUseCase @Inject constructor() {

    operator fun invoke(
        stores: List<Store>,
        userLocation: UserLocation,
        radiusKm: Double = 5.0
    ): List<Store> {
        return stores.filter { store ->
            val distance = userLocation.distanceTo(store)
            distance <= radiusKm
        }
    }

    fun getStoresWithDistances(
        stores: List<Store>,
        userLocation: UserLocation,
        radiusKm: Double = 5.0
    ): List<Pair<Store, Double>> {
        return stores.mapNotNull { store ->
            val distance = userLocation.distanceTo(store)
            if (distance <= radiusKm) {
                Pair(store, distance)
            } else {
                null
            }
        }.sortedBy { it.second } // Sort by distance
    }
}
