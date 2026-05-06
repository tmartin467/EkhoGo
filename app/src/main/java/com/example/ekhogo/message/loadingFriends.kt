package com.example.ekhogo.message

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


data class FriendUser(
    val id: String,
    val name: String =""
)


fun loadingFriends(uid: String, onResult: (List<String>) -> Unit) {

    val db = FirebaseFirestore.getInstance()

    db.collection("friend_requests")
        .whereEqualTo("status", "accepted")
        .get()
        .addOnSuccessListener { snapshot ->

            val friendIds = snapshot.documents.mapNotNull { doc ->

                val fromUserId = doc.getString("fromUserId")
                val toUserId = doc.getString("toUserId")

                when (uid) {
                    fromUserId -> toUserId
                    toUserId -> fromUserId
                    else -> null
                }
            }

            onResult(friendIds)
        }
}