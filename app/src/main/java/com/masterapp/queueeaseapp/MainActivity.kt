package com.masterapp.queueeaseapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.*
import com.masterapp.queueeaseapp.ui.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "queue_channel",
                "Queue Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        setContent {

            val navController = rememberNavController()

            // 🔥 TEMP ROLE
            val role = "USER"   // change ADMIN/USER for testing

            NavHost(
                navController = navController,
                startDestination = "login"
            ) {

                // ✅ LOGIN
                composable("login") {
                    LoginScreen { uid ->

                        navController.navigate("centers/$uid") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }

                // ✅ CENTER LIST
                composable("centers/{userId}") { backStackEntry ->

                    val userId = backStackEntry.arguments
                        ?.getString("userId")!!.toLong()

                    CenterListScreen(
                        userId = userId,
                        role = role,
                        onCenterClick = { centerId ->
                            navController.navigate("centerDetail/$userId/$centerId")
                        },
                        onAddCenterClick = {}
                    )
                }

                // ✅ CENTER DETAIL
                composable("centerDetail/{userId}/{centerId}") { backStackEntry ->

                    val userId = backStackEntry.arguments
                        ?.getString("userId")!!.toLong()

                    val centerId = backStackEntry.arguments
                        ?.getString("centerId")!!.toLong()

                    CenterDetailScreen(
                        userId = userId,
                        centerId = centerId,
                        role = role,
                        onJoinSuccess = {
                            navController.navigate("queueStatus/$userId/$centerId")
                        },
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // ✅ QUEUE STATUS
                composable("queueStatus/{userId}/{centerId}") { backStackEntry ->

                    val userId = backStackEntry.arguments
                        ?.getString("userId")!!.toLong()

                    val centerId = backStackEntry.arguments
                        ?.getString("centerId")!!.toLong()

                    QueueStatusScreen(userId, centerId)
                }
            }
        }
    }
}