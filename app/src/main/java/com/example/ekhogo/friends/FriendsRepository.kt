package com.example.ekhogo.friends

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendsRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun sendFriendRequest(toUserId: String, onComplete: (Boolean) -> Unit = {}) {
        val currentUserId = auth.currentUser?.uid ?: run {
            onComplete(false)
            return
        }

        val requestId = "${currentUserId}_${toUserId}"
        val request = hashMapOf(
            "fromUserId" to currentUserId,
            "toUserId" to toUserId,
            "status" to "pending",
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("friend_requests")
            .document(requestId)
            .set(request)
            .addOnSuccessListener {
                Log.d("FRIENDS", "Friend request sent")
                onComplete(true)
            }
            .addOnFailureListener {
                Log.e("FRIENDS", "Error sending request", it)
                onComplete(false)
            }
    }

    fun loadUsers(onResult: (List<Friend>) -> Unit) {
        val currentUserId = auth.currentUser?.uid ?: run {
            onResult(emptyList())
            return
        }

        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { currentUserDocument ->
                val currentFriends = (currentUserDocument.get("friends") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toSet()
                    ?: emptySet()

                db.collection("friend_requests")
                    .whereEqualTo("toUserId", currentUserId)
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener { incomingSnapshot ->
                            val incomingRequestUserIds = incomingSnapshot.documents.mapNotNull { document->
                                document.getString("fromUserId")
                            }.toSet()

                            db.collection("friend_requests")
                                .whereEqualTo("fromUserId", currentUserId)
                                .whereEqualTo("status", "pending")
                                .get()
                                .addOnSuccessListener { outgoingSnapshot ->
                                    val outgoingRequestUserIds = outgoingSnapshot.documents.mapNotNull { document ->
                                        document.getString("toUserId")
                                    }.toSet()

                                    db.collection("users")
                                        .get()
                                        .addOnSuccessListener { usersSnapshot ->
                                            val users = usersSnapshot.documents.mapNotNull { document ->
                                                val uid = document.getString("uid") ?: document.id
                                                val name = document.getString("name") ?: return@mapNotNull null
                                                val major = document.getString("major") ?: ""

                                                if (uid == currentUserId) {
                                                    return@mapNotNull null
                                                }

                                                val status = when {
                                                    currentFriends.contains(uid) -> FriendStatus.FRIENDS
                                                    incomingRequestUserIds.contains(uid) -> FriendStatus.REQUEST_RECEIVED
                                                    outgoingRequestUserIds.contains(uid) -> FriendStatus.REQUEST_SENT
                                                    else -> FriendStatus.NONE
                                                }

                                                Friend(
                                                    id = uid,
                                                    name = name,
                                                    major = major,
                                                    status = status
                                                )
                                            }.sortedBy { it.name.lowercase() }

                                            onResult(users)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("FRIENDS", "Error loading users", e)
                                            onResult(emptyList())
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FRIENDS", "Error loading outgoing requests", e)
                                    onResult(emptyList())
                                }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FRIENDS", "Error loading incoming requests", e)
                        onResult(emptyList())
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FRIENDS", "Error loading current user", e)
                onResult(emptyList())
            }
    }

    fun acceptFriendRequest(fromUserId: String, onComplete: (Boolean) -> Unit = {}) {
        val currentUserId = auth.currentUser?.uid ?: run {
            onComplete(false)
            return
        }

        val requestId =  "$(fromUserId)_${currentUserId}"
        val requestRef = db.collection("friend_requests").document(requestId)
        val currentUserRef = db.collection("users").document(currentUserId)
        val otherUserRef = db.collection("users").document(fromUserId)

        val batch = db.batch()
        batch.update(
            requestRef,
            mapOf<String, Any>(
                "status" to "accepted",
                "respondedAt" to FieldValue.serverTimestamp()
            )
        )
        batch.update(currentUserRef, "friends", FieldValue.arrayUnion(fromUserId))
        batch.update(otherUserRef, "friends", FieldValue.arrayUnion(currentUserId))

        batch.commit()
            .addOnSuccessListener {
                Log.d("FRIENDS", "Friend request accepted")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FRIENDS", "Error accepting friend request", e)
                onComplete(false)
            }
    }

    fun rejectFriendRequest(fromUserId: String, onComplete: (Boolean) -> Unit = {}) {
        val currentUserId = auth.currentUser?.uid ?: run {
            onComplete(false)
            return
        }

        val requestId = "$(fromUserId)_${currentUserId}"

        db.collection("friend_requests")
            .document(requestId)
            .update(
                mapOf<String, Any>(
                    "status" to "rejected",
                    "respondedAt" to FieldValue.serverTimestamp()
                )
            )
            .addOnSuccessListener {
                Log.d("FRIENDS", "Friend request rejected")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.d("FRIENDS", "Error rejecting friend request", e)
                onComplete(false)
            }
    }

    fun removeFriend(fromUserId: String, onComplete: (Boolean) -> Unit = {}) {
        val currentUserId = auth.currentUser?.uid ?: run {
            onComplete(false)
            return
        }

        val currentUserRef = db.collection("users").document(currentUserId)
        val otherUserRef = db.collection("users").document(fromUserId)

        val batch = db.batch()
        batch.update(currentUserRef, "friends", FieldValue.arrayRemove(fromUserId))
        batch.update(otherUserRef, "friends", FieldValue.arrayRemove(currentUserId))

        batch.commit()
            .addOnSuccessListener {
                Log.d("FRIENDS", "Friend removed")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.d("FRIENDS", "Error removing friend", e)
                onComplete(false)
            }
    }

}