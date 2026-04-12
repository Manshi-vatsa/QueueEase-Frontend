package com.masterapp.queueeaseapp

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

        setContent {
            QueueEaseAppTheme {

                // ✅ FIRST define state
                var currentScreen by remember { mutableStateOf("login") }
                var userId by remember { mutableStateOf(1L) }
                var selectedCenterId by remember { mutableStateOf(1L) }

                // ✅ THEN use it in BackHandler
                BackHandler {
                    when (currentScreen) {
                        "status" -> currentScreen = "center"
                        "center" -> currentScreen = "login"
                        else -> finish()
                    }
                }

                // ✅ Navigation logic
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