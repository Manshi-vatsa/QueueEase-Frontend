package com.masterapp.queueeaseapp

import com.masterapp.queueeaseapp.api.joinQueue
import android.util.Log
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CenterListScreen(
    userId: Long,
    onJoinSuccess: () -> Unit
) {

    val centers = listOf(
        Center(1, "Bank"),
        Center(2, "Hospital"),
        Center(3, "Passport Office")
    )

    var selectedCenter by remember { mutableStateOf<Center?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(20.dp)) {

        Text("Select Service Center")

        Spacer(modifier = Modifier.height(10.dp))

        Box {
            Button(onClick = { expanded = true }) {
                Text(selectedCenter?.name ?: "Select Center")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                centers.forEach {
                    DropdownMenuItem(
                        text = { Text(it.name) },
                        onClick = {
                            selectedCenter = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {

            if (selectedCenter == null) {
                Log.e("JOIN_ERROR", "Center not selected")
                return@Button
            }

            val centerId = selectedCenter!!.id.toLong()   // ✅ FIX

            Log.d(
                "JOIN_DEBUG",
                "UserId: $userId CenterId: $centerId"
            )

            joinQueue(userId, centerId)   // ✅ FIXED

            onJoinSuccess()

        }) {
            Text("Join Queue")
        }
    }
}

data class Center(
    val id: Int,
    val name: String
)