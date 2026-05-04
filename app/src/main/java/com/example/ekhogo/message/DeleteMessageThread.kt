package com.example.ekhogo.message

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


fun deleteMessageThread(otherUserId: String){

        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return


        val conversationId = listOf(uid, otherUserId)
            .sorted()
            .joinToString("_")

    val doc= db.collection("conversations")
        .document(conversationId)

    doc.update("deletedFor", FieldValue.arrayUnion(uid))
        .addOnSuccessListener {
            doc.get().addOnSuccessListener { document ->
                val deletedFor = document.get("deletedFor") as? List<String> ?: emptyList()
                val participants = document.getLong("numOfParticipants")

                if (deletedFor.size.toLong() == participants) {

                    val messagesRef = doc.collection("messages")

                    messagesRef.get().addOnSuccessListener { snapshot ->
                        val batch = db.batch()

                        for (message in snapshot.documents) {
                            batch.delete(message.reference)
                        }

                        batch.commit().addOnSuccessListener {
                            doc.delete()
                        }
                    }


                   doc.delete()
                }
            }
        }

}