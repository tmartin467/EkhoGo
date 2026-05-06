package com.example.ekhogo.message

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

fun deleteMessageThread(conversationId: String) {

    val db = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val docRef = db.collection("conversations").document(conversationId)

    docRef.update("deletedFor", FieldValue.arrayUnion(uid))
        .addOnSuccessListener {

            docRef.get().addOnSuccessListener { document ->

                val deletedFor = document.get("deletedFor") as? List<String> ?: emptyList()
                val participants = document.get("participants") as? List<String> ?: emptyList()

                val allDeleted = participants.all { it in deletedFor }

                if (allDeleted) {

                    docRef.collection("messages")
                        .get()
                        .addOnSuccessListener { snapshot ->

                            val batch = db.batch()

                            snapshot.documents.forEach { message ->
                                batch.delete(message.reference)
                            }

                            batch.commit().addOnSuccessListener {
                                docRef.delete()
                            }
                        }
                }
            }
        }
}