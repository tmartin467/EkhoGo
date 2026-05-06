package com.example.ekhogo.notifications

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging

object MessagingTokenRepository {
    private const val TAG = "MessagingTokens"

    fun saveCurrentUserToken() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                val tokenData = mapOf(
                    "token" to token,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("fcmTokens")
                    .document(token)
                    .set(tokenData, SetOptions.merge())
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Unable to save FCM token", error)
            }
    }

    fun deleteCurrentUserToken(onComplete: () -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                val token = if (task.isSuccessful) task.result else null
                if (uid.isNullOrBlank() || token.isNullOrBlank()) {
                    onComplete()
                    return@addOnCompleteListener
                }

                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("fcmTokens")
                    .document(token)
                    .delete()
                    .addOnCompleteListener {
                        onComplete()
                    }
            }
    }
}
