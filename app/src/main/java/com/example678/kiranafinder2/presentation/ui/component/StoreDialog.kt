package com.example678.kiranafinder2.presentation.ui.component

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import coil.compose.AsyncImage
import com.example678.kiranafinder2.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StoreDialog(
    store: Store,
    uiState: MapUiState, // ✅ Add this parameter
    onDismiss: () -> Unit,
    onUpdateStatus: (StoreStatus, String) -> Unit
) {
    var note by remember { mutableStateOf("") }
    var isVisible by remember { mutableStateOf(false) }

    Log.d("StoreDialog", "🎯 Showing dialog for: ${store.name}")

    LaunchedEffect(store) { isVisible = true }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .shadow(24.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface.copy(0.95f)
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    // 🎯 HEADER: Store Name + Status Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = store.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (store.address.isNotEmpty()) {
                                Text(
                                    text = store.address,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (store.status) {
                                StoreStatus.OPEN -> Color(0xFF4CAF50)
                                StoreStatus.CLOSED -> Color(0xFFE57373)
                                else -> Color(0xFFFFB74D)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (store.status) {
                                        StoreStatus.OPEN -> Icons.Default.CheckCircle
                                        StoreStatus.CLOSED -> Icons.Default.Cancel
                                        else -> Icons.Default.Help
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = store.status.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🎨 COMMUNITY NOTE (if exists)
                    if (store.lastNote.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Default.ChatBubble, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(store.lastNote, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 🎯 USER ATTRIBUTION (Compact)
                    if (store.reportedByName.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = store.reportedByPhotoUrl.ifEmpty { "https://via.placeholder.com/24" },
                                contentDescription = null,
                                modifier = Modifier.size(24.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${store.reportedByName} • ${formatTimeAgo(store.lastUpdated)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.weight(1f))
                            if (store.verificationScore > 0) {
                                Icon(Icons.Default.Verified, null, modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 🎨 NOTE INPUT (Compact)
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("Add your note...", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🚀 ACTION BUTTONS (Enhanced)
                    // 🚀 ACTION BUTTONS (Fixed - All buttons in same row)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { // Reduced spacing
                        // Dismiss Button
                        OutlinedButton(
                            onClick = { Log.d("StoreDialog", "❌ Cancel clicked"); onDismiss() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp, maxLines = 1)
                        }

                        // Open Button
                        // Open Button - WITH LOADING STATE
                        Button(
                            onClick = {
                                Log.d("StoreDialog", "🔥 CONFIRM OPEN: ${store.name}")
                                onUpdateStatus(StoreStatus.OPEN, note)
                            },
                            enabled = !uiState.isUpdatingStore, // ✅ Add this line
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            // ✅ Add loading indicator
                            if (uiState.isUpdatingStore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }


                        // Closed Button - FIXED
                        // Closed Button - WITH LOADING STATE
                        Button(
                            onClick = {
                                Log.d("StoreDialog", "🔥 CONFIRM CLOSED: ${store.name}")
                                onUpdateStatus(StoreStatus.CLOSED, note)
                            },
                            enabled = !uiState.isUpdatingStore, // ✅ Add this line
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            // ✅ Add loading indicator
                            if (uiState.isUpdatingStore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Cancel, null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Closed", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }

                    }

                }
            }
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}
