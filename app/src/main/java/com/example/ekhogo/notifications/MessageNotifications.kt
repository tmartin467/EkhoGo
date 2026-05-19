package com.example.ekhogo.notifications

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.ekhogo.MainActivity
import com.example.ekhogo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

object MessageNotifications {
    private const val TAG = "MessageNotifications"
    private const val CHANNEL_ID = "messages"
    private const val CHANNEL_NAME = "Messages"
    private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 204

    fun prepare(context: Context) {
        createChannel(context)

        if (context is Activity) {
            requestPermissionIfNeeded(context)
        }
    }

    fun saveCurrentToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                saveToken(token)
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Could not get FCM token", error)
            }
    }

    fun saveToken(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(
                mapOf(
                    "fcmTokens" to FieldValue.arrayUnion(token),
                    "fcmTokenUpdatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .addOnFailureListener { error ->
                Log.w(TAG, "Could not save FCM token", error)
            }
    }

    fun removeCurrentToken(onComplete: () -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            onComplete()
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { tokenTask ->
                val token = tokenTask.result
                if (!tokenTask.isSuccessful || token.isNullOrBlank()) {
                    onComplete()
                    return@addOnCompleteListener
                }

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("fcmTokens", FieldValue.arrayRemove(token))
                    .addOnCompleteListener {
                        onComplete()
                    }
            }
    }

    fun showMessageNotification(
        context: Context,
        title: String,
        body: String,
        conversationId: String?,
        notificationId: String?
    ) {
        createChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val resolvedNotificationId = notificationId
            ?: conversationId
            ?: System.currentTimeMillis().toString()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("conversationId", conversationId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            resolvedNotificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(resolvedNotificationId.hashCode(), notification)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "New message notifications"
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)
    }

    private fun requestPermissionIfNeeded(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST_CODE
        )
    }
}
