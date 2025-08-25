package com.example678.kiranafinder2.domain.usecase

import com.example678.kiranafinder2.domain.model.StoreStatus
import com.example678.kiranafinder2.domain.repository.StoreRepository
import javax.inject.Inject

class UpdateStoreStatusUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(
        storeId: String,
        status: StoreStatus,
        note: String = "",
        userId: String = "current_user"
    ) {
        repository.updateStoreStatus(storeId, status, note, userId)
    }
}
