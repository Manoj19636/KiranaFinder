package com.example678.kiranafinder2.data.model

import com.example678.kiranafinder2.domain.model.Store
import com.example678.kiranafinder2.domain.model.StoreStatus

data class StoreEntity(
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "UNKNOWN",
    val address: String = "",
    val lastNote: String = "",
    val lastUpdated: com.google.firebase.Timestamp? = null,
    val reportedBy: String = ""
)

// Extension functions to convert between domain and data models
fun StoreEntity.toDomain(): Store {
    return Store(
        id = name.hashCode().toString(),
        name = name.trim().ifBlank { "Unknown Store" }, // Handle empty names
        latitude = latitude,
        longitude = longitude,
        status = try {
            StoreStatus.valueOf(status.uppercase().trim()) // Handle case issues
        } catch (e: Exception) {
            StoreStatus.UNKNOWN
        },
        address = address.trim(),
        lastUpdated = lastUpdated?.toDate()?.time ?: System.currentTimeMillis(),
        lastNote = lastNote.trim(),
        reportedBy = reportedBy.trim()
    )
}


fun Store.toEntity(): StoreEntity {
    return StoreEntity(
        name = name,
        latitude = latitude,
        longitude = longitude,
        status = status.name,
        address = address,
        lastNote = lastNote,
        lastUpdated = com.google.firebase.Timestamp(java.util.Date(lastUpdated)),
        reportedBy = reportedBy
    )
}
