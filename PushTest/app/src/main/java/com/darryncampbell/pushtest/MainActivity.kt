package com.darryncampbell.pushtest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.bold
import androidx.core.text.underline
import com.darryncampbell.pushtest.ui.theme.PushTestTheme
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    private val LOG_TAG = "PushTest"
    private var pushNotificationPermission = true
    private val pushNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted)
        {
            Log.e(LOG_TAG, "Notification permissions were not granted")
            pushNotificationPermission = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        getToken()
        pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        setContent {
            PushTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    private fun getToken()
    {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            try {
                val token = task.result
                if (token != null) {
                    Log.d(LOG_TAG, "Retrieved token: $token");
                }
            } catch (e: Exception) {
                //  This issue seems to happen on a newly launched emulator through Appetize.io ('switching to device').  Seems to be a known issue.
                //  https://stackoverflow.com/questions/62562243/java-io-ioexception-authentication-failed-in-android-firebase-and-service-not
                Handler(Looper.getMainLooper()).postDelayed(Runnable { getToken(); }, 1000)
            }
        }
    }

    private fun createNotificationChannel() {
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.default_notification_channel_name)
            val descriptionText = getString(R.string.default_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel( getString(R.string.default_notification_channel_id),name, importance).apply {
                description = descriptionText
            }
            //Channel settings
            channel.description = descriptionText
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Please see adb logs for Push token",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PushTestTheme {
        Greeting("Android")
    }
}