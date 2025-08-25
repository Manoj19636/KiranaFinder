package com.example678.kiranafinder2.presentation.ui.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example678.kiranafinder2.domain.model.Store
import com.example678.kiranafinder2.domain.model.StoreStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StoreDialog(
    store: Store,
    onDismiss: () -> Unit,
    onUpdateStatus: (StoreStatus, String) -> Unit
) {
    var note by remember { mutableStateOf("") }

    Log.d("StoreDialog", "🎯 Showing dialog for: ${store.name}")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = store.name,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Store Info
                StoreBasicInfo(store = store)

                Spacer(modifier = Modifier.height(16.dp))

                // Last Update Attribution
                if (store.reportedByName.isNotEmpty()) {
                    LastUpdateAttribution(store = store)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Community Note Input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Add your note (optional)") },
                    placeholder = { Text("e.g., Fresh milk available, Best prices!") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Column {
                // Status Update Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            Log.d("StoreDialog", "🔥 CONFIRM OPEN clicked for: ${store.name}")
                            onUpdateStatus(StoreStatus.OPEN, note)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm Open")
                    }

                    Button(
                        onClick = {
                            Log.d("StoreDialog", "🔥 CONFIRM CLOSED clicked for: ${store.name}")
                            onUpdateStatus(StoreStatus.CLOSED, note)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm Closed")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                Log.d("StoreDialog", "❌ Cancel clicked")
                onDismiss()
            }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StoreBasicInfo(store: Store) {
    Column {
        Text(
            text = "Status: ${store.status.name}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        if (store.address.isNotEmpty()) {
            Text(
                text = "Address: ${store.address}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Community Note Display
        if (store.lastNote.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💬 Community Note:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = store.lastNote,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun LastUpdateAttribution(store: Store) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Photo
            AsyncImage(
                model = store.reportedByPhotoUrl.ifEmpty { "https://via.placeholder.com/32" },
                contentDescription = "Reporter Photo",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Attribution Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Last updated by ${store.reportedByName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = formatTimeAgo(store.lastUpdated),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Trust Score (if available)
            if (store.verificationScore > 0) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "✓ Verified",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
