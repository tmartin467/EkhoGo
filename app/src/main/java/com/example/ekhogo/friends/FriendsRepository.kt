package com.example.ekhogo.friends

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun sendFriendRequest(toUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        val request = hashMapOf(
            "fromUserId" to currentUserId,
            "toUserId" to toUserId,
            "status" to "pending",
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("friend_requests")
            .add(request)
            .addOnSuccessListener {
                Log.d("FRIENDS", "Friend request sent")
            }
            .addOnFailureListener {
                Log.e("FRIENDS", "Error sending request", it)
            }
    }

    fun loadUsers(
        onResult: (List<Friend>) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection
    }

}