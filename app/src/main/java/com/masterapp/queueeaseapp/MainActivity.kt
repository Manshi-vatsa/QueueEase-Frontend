package com.masterapp.queueeaseapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.masterapp.queueeaseapp.ui.CenterListScreen
import com.masterapp.queueeaseapp.ui.CenterDetailScreen
import com.masterapp.queueeaseapp.ui.QueueStatusScreen

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

            // 🔥 TEMP FIX
            var role by remember { mutableStateOf("ADMIN") }

            // ❗ FIXED USER ID
            val userId = 1L

            NavHost(
                navController = navController,
                startDestination = "centers"
            ) {

                composable("centers") {
                    CenterListScreen(
                        userId = userId,
                        role = role,
                        onCenterClick = { centerId ->
                            navController.navigate("queueStatus/$userId/$centerId")
                        },
                        onAddCenterClick = {
                            // TODO: Add Center screen
                        }
                    )
                }

                composable("centerDetail/{centerId}") { backStackEntry ->
                    val centerId =
                        backStackEntry.arguments?.getString("centerId")!!.toLong()

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

                composable("queueStatus/{userId}/{centerId}") { backStackEntry ->

                    val uid = backStackEntry.arguments?.getString("userId")!!.toLong()
                    val cid = backStackEntry.arguments?.getString("centerId")!!.toLong()

                    QueueStatusScreen(uid, cid)
                }
            }
        }
    }
}