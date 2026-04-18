package com.masterapp.queueeaseapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val QUEUE_CHANNEL_ID = "queue_channel"
    private const val CROWD_CHANNEL_ID = "crowd_channel"
    private const val QUEUE_NOTIFICATION_ID = 1001
    private const val CROWD_NOTIFICATION_ID = 1002

    fun initializeChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Queue notifications channel
            val queueChannel = NotificationChannel(
                QUEUE_CHANNEL_ID,
                "Queue Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for queue updates and turn reminders"
                enableLights(true)
                enableVibration(true)
            }

            // Crowd level notifications channel
            val crowdChannel = NotificationChannel(
                CROWD_CHANNEL_ID,
                "Crowd Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warnings for high crowd areas"
                enableLights(true)
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(queueChannel)
            manager.createNotificationChannel(crowdChannel)
        }
    }

    fun showQueueNotification(context: Context, message: String = "Your turn is approaching") {
        initializeChannels(context)

        val builder = NotificationCompat.Builder(context, QUEUE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("QueueEase - Queue Update")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(QUEUE_NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }

    fun showCrowdWarning(context: Context, areaName: String) {
        initializeChannels(context)

        val builder = NotificationCompat.Builder(context, CROWD_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("⚠️ High Crowd Alert")
            .setContentText("$areaName is experiencing high crowd levels")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("⚠️ Warning: $areaName is currently experiencing high crowd levels. Consider visiting later or choosing an alternative location.")
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                "View Alternatives",
                null // You can add a pending intent here
            )

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(CROWD_NOTIFICATION_ID + areaName.hashCode(), builder.build())
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }

    fun showGeneralNotification(context: Context, title: String, message: String) {
        initializeChannels(context)

        val builder = NotificationCompat.Builder(context, QUEUE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
        }
    }
}

// Legacy function for backward compatibility
fun showNotification(context: Context) {
    NotificationHelper.showQueueNotification(context)
}