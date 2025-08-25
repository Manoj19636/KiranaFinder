package com.example678.kiranafinder2.domain.usecase



import com.example678.kiranafinder2.domain.model.UserLocation
import com.example678.kiranafinder2.domain.repository.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): Result<UserLocation> {
        return locationRepository.getCurrentLocation()
    }
}
