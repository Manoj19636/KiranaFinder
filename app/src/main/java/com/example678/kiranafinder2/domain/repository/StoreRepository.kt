package com.example678.kiranafinder2.domain.repository



import com.example678.kiranafinder2.domain.model.Store
import com.example678.kiranafinder2.domain.model.StatusUpdate
import com.example678.kiranafinder2.domain.model.StoreStatus
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun getStoresNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 2.0
    ): Flow<List<Store>>

    suspend fun updateStoreStatus(
        storeId: String,
        status: StoreStatus,
        note: String,
        userId: String
    )

    suspend fun addStore(store: Store) // Add this method
}

