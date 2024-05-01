package com.darryncampbell.pushtest

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMHandler : FirebaseMessagingService() {
    private val LOG_TAG = "PushTest"

    override fun onNewToken(token: String) {
        //Called whenever the FCM token is renewed - re-register the device with PubNub
        Log.d(LOG_TAG, "New Token Received: $token")
    }

    //Handle when the device has received a push notification from FCM.
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val clientIdentifier = applicationContext.getSharedPreferences("prefs.db", 0).getString("identifier", null)
        var title : String
        var body : String

        Log.d(LOG_TAG, "Message Received from FCM")

        // Check if message contains a data payload.
        //  This is always handled by the application regardless of foreground / background.
        //  Display the notification ourselves
        if (remoteMessage.data.isNotEmpty()) {
            remoteMessage.data.let {
                Log.d(LOG_TAG, "Message data payload: " + remoteMessage.data)
                title = remoteMessage.data.get("title").toString()
                body = remoteMessage.data.get("body").toString()
                sendNotification(title, body)
            }
        }

        // Check if message contains a notification payload.
        //  This will never be the case when the app is in the background (since the
        //  notification is handled automatically by FCM).
        //  If we are in the foreground, display a snackbar rather than a notification
        remoteMessage.notification?.let {
            Log.d(LOG_TAG, "Message Notification Body: ${it.body}")
            title = remoteMessage.notification?.title.toString()
            body = remoteMessage.notification?.body.toString()

            sendNotification(title, body)
        }
    }

    private fun sendNotification(title: String, body: String) {
        val notificationID = 101
        //  Just launch the app if the notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pend = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val channelID = getString(R.string.default_notification_channel_id)

        var builder = NotificationCompat.Builder(this, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(resources.getColor(R.color.purple_500, theme))
            .setContentIntent(pend)
            .setAutoCancel(true)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationID, builder.build())
    }
}