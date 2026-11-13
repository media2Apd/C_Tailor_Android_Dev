package com.cuso.tailor.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cuso.tailor.MainActivity
import com.cuso.tailor.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.content.edit

class CusoFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID = "cuso_general_notifications"
        const val CHANNEL_NAME = "General Notifications"
        const val EXTRA_OPEN_SCREEN = "open_screen"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Refreshed FCM token: $token")
        // Save locally so HomeScreen / ViewModel can sync it to backend if user is logged in
        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
            .edit {
                putString("fcm_token", token)
                    .putBoolean("fcm_synced", false)
            }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "CUSO Tailor"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: remoteMessage.data["body"]
            ?: ""

        showSystemNotification(
            title = title,
            message = body,
            notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        )
    }

    private fun showSystemNotification(
        title: String,
        message: String,
        notificationId: Int
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create NotificationChannel for Android 8.0+ (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time order, inventory, and business alerts"
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }

        // Open MainActivity when user taps the system notification
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_SCREEN, "notifications")
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Ensure this is a monochrome vector/drawable icon
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(notificationId, notification)
    }
}