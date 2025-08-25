package com.example678.kiranafinder2.domain.model



data class Store(
    // Your existing fields...
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val status: StoreStatus,
    val address: String,
    val lastUpdated: Long,
    val lastNote: String,

    // Enhanced attribution fields
    val reportedBy: String = "",
    val reportedByName: String = "",  // Display name for UI
    val reportedByPhotoUrl: String = "",  // Profile photo
    val updateCount: Int = 1,  // How many times this store has been updated
    val verificationScore: Float = 0.0f , // Community trust score,
    // 🆕 NEW: Track contribution type
    val contributionType: String = "status_update" // "new_store", "status_update", "note_added"
)


enum class StoreStatus {
    OPEN, CLOSED, UNKNOWN
}

data class StatusUpdate(
    val storeId: String,
    val status: StoreStatus,
    val timestamp: Long,
    val userId: String,
    val note: String = ""
)
