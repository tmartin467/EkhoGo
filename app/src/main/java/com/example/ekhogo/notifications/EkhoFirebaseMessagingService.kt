package com.example.ekhogo.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class EkhoFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        MessageNotifications.saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "New message"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["messageText"]
            ?: "Open EkhoGo to read it."

        MessageNotifications.showMessageNotification(
            context = this,
            title = title,
            body = body,
            conversationId = message.data["conversationId"],
            notificationId = message.data["notificationId"] ?: message.messageId
        )
    }
}
