package com.example678.kiranafinder2.presentation.ui.component



import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng

@Composable
fun AddNewStoreDialog(
    location: LatLng?,
    onDismiss: () -> Unit,
    onAddStore: (String, String) -> Unit
) {
    var storeName by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }

    if (location != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text("🆕 Add New Store")
            },
            text = {
                Column {
                    Text("Location: ${String.format("%.4f", location.latitude)}, ${String.format("%.4f", location.longitude)}")

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Store Name *") },
                        placeholder = { Text("e.g., Ram's Kirana Store") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = storeAddress,
                        onValueChange = { storeAddress = it },
                        label = { Text("Address (optional)") },
                        placeholder = { Text("e.g., Near Bus Stop, Block A") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (storeName.isNotBlank()) {
                            onAddStore(storeName, storeAddress)
                        }
                    },
                    enabled = storeName.isNotBlank()
                ) {
                    Text("Add Store")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
