package com.example678.kiranafinder2.domain.usecase



import com.example678.kiranafinder2.domain.model.Store
import com.example678.kiranafinder2.domain.repository.StoreRepository
import javax.inject.Inject

class AddNewStoreUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(store: Store) {
        repository.addStore(store)
    }
}
