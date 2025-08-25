package com.example678.kiranafinder2.domain.usecase



import com.example678.kiranafinder2.domain.model.Store
import com.example678.kiranafinder2.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNearbyStoresUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    operator fun invoke(latitude: Double, longitude: Double, radiusKm: Double = 2.0): Flow<List<Store>> {
        return repository.getStoresNearLocation(latitude, longitude, radiusKm)
    }
}
