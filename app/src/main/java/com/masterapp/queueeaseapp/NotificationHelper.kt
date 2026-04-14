package com.masterapp.queueeaseapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

fun showNotification(context: Context) {
    val channelId = "queue_channel"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Queue Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("QueueEase")
        .setContentText("Your turn is approaching")
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    manager.notify(1001, builder.build())
}