package com.example678.kiranafinder2.data.repository

import android.util.Log
import com.example678.kiranafinder2.data.model.StoreEntity
import com.example678.kiranafinder2.data.model.toDomain
import com.example678.kiranafinder2.data.model.toEntity
import com.example678.kiranafinder2.domain.model.Store
import com.example678.kiranafinder2.domain.model.StoreStatus
import com.example678.kiranafinder2.domain.repository.StoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepositoryImpl @Inject constructor() : StoreRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storesCollection = firestore.collection("stores")

    companion object {
        private const val TAG = "StoreRepository"
    }

    override fun getStoresNearLocation(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Flow<List<Store>> = callbackFlow {
        Log.d(TAG, "🔍 Setting up real-time listener for stores...")

        val listener = storesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "❌ Real-time listener error: ${error.message}")
                cancel()
                return@addSnapshotListener
            }

            if (snapshot != null) {
                Log.d(TAG, "🔄 Real-time update received: ${snapshot.documents.size} documents")

                val stores = mutableListOf<Store>()

                snapshot.documents.forEach { document ->
                    try {
                        val storeEntity = document.toObject<StoreEntity>()
                        if (storeEntity != null && storeEntity.name.isNotBlank()) {
                            val domainStore = storeEntity.toDomain()

                            // Filter out invalid stores
                            if (domainStore.name != "Unknown Store" &&
                                domainStore.name.isNotBlank() &&
                                domainStore.latitude != 0.0 &&
                                domainStore.longitude != 0.0) {

                                stores.add(domainStore)
                                Log.d(TAG, "✅ Real-time store: ${domainStore.name}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error processing document ${document.id}: ${e.message}")
                    }
                }

                Log.d(TAG, "🔄 Emitting ${stores.size} stores via real-time listener")
                trySend(stores)
            }
        }

        // Clean up listener when Flow is cancelled
        awaitClose {
            Log.d(TAG, "🔥 Removing real-time listener")
            listener.remove()
        }
    }


    override suspend fun updateStoreStatus(
        storeId: String,
        status: StoreStatus,
        note: String,
        userId: String
    ) {
        try {
            Log.d(TAG, "🔄 Starting update for storeId: $storeId")

            // Get all stores and find the one that matches the ID
            val snapshot = storesCollection.get().await()

            for (document in snapshot.documents) {
                val storeEntity = document.toObject<StoreEntity>()
                if (storeEntity != null) {
                    val domainStore = storeEntity.toDomain()
                    if (domainStore.id == storeId) {
                        Log.d(TAG, "🔄 Found matching store: ${domainStore.name}")

                        val updates = mapOf(
                            "status" to status.name,
                            "lastNote" to note,
                            "lastUpdated" to com.google.firebase.Timestamp.now(),
                            "reportedBy" to userId
                        )

                        document.reference.update(updates).await()
                        Log.d(TAG, "✅ Store status updated successfully: ${domainStore.name} to $status")
                        return
                    }
                }
            }

            Log.e(TAG, "❌ No store found with ID: $storeId")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error updating store status: ${e.message}")
            e.printStackTrace()
        }
    }



    // Helper function to map store IDs to names
    private fun getStoreNameById(storeId: String): String {
        // This mapping is wrong! Your actual store objects use name.hashCode() as ID
        // Let's fix this by using a different approach
        return when (storeId) {
            "Sharma Kirana Store".hashCode().toString() -> "Sharma Kirana Store"
            "Gupta General Store".hashCode().toString() -> "Gupta General Store"
            "Fresh Vegetables".hashCode().toString() -> "Fresh Vegetables"
            else -> {
                Log.e(TAG, "❌ Unknown storeId: $storeId")
                ""
            }
        }
    }
    override suspend fun addStore(store: Store) {
        try {
            Log.d(TAG, "🆕 Adding new store to Firestore: ${store.name}")

            val storeEntity = store.toEntity()
            storesCollection.add(storeEntity).await()

            Log.d(TAG, "✅ New store added successfully: ${store.name}")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error adding new store: ${e.message}")
            throw e
        }
    }


}
