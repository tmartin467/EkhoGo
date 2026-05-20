package com.example.ekhogo.message

import com.google.firebase.firestore.FirebaseFirestore

fun loadingOwnName(uid: String, onResult: (String) -> Unit) {



        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                 val name = doc.getString("name") ?: "Unknown"
                 onResult(name)
            }




}
