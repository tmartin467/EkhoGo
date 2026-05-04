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

        db.collection("conversations")
            .document(conversationId)
            .update(
                "deletedFor", FieldValue.arrayUnion(uid)
            )







}