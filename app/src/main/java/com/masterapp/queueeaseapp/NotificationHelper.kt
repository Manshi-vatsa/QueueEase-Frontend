package com.masterapp.queueeaseapp

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

fun showNotification(context: Context) {

    val builder = NotificationCompat.Builder(context, "queue_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("QueueEase")
        .setContentText("Your turn is approaching")
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    manager.notify(1, builder.build())
}