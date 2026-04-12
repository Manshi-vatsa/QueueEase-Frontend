package com.masterapp.queueeaseapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.masterapp.queueeaseapp.ui.*
import com.masterapp.queueeaseapp.ui.theme.QueueEaseAppTheme
import androidx.activity.compose.BackHandler

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // ✅ ADD THIS BLOCK (ONLY THIS IS NEW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "queue_channel",
                "Queue Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            if (android.os.Build.VERSION.SDK_INT >= 33) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        setContent {
            QueueEaseAppTheme {

                var currentScreen by remember { mutableStateOf("login") }
                var userId by remember { mutableStateOf(1L) }
                var selectedCenterId by remember { mutableStateOf(1L) }

                BackHandler {
                    when (currentScreen) {
                        "status" -> currentScreen = "center"
                        "center" -> currentScreen = "login"
                        else -> finish()
                    }
                }

                when (currentScreen) {

                    "login" -> LoginScreen { id ->
                        userId = id
                        currentScreen = "center"
                    }

                    "center" -> CenterListScreen(userId) { centerId ->
                        selectedCenterId = centerId
                        currentScreen = "status"
                    }

                    "status" -> StatusScreen(userId, selectedCenterId)
                }
            }
        }
    }
}