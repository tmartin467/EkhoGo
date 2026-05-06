package com.example.ekhogo.message

import com.google.firebase.firestore.FirebaseFirestore


fun loadingNames(
    ids: List<String>,
    onResult: (List<FriendUser>) -> Unit
) {

    val db = FirebaseFirestore.getInstance()
    val results = mutableListOf<FriendUser>()

    if (ids.isEmpty()) {
        onResult(emptyList())
        return
    }

    ids.forEach { id ->

        db.collection("users")
            .document(id)
            .get()
            .addOnSuccessListener { doc ->

                val name = doc.getString("name") ?: "Unknown"

                results.add(FriendUser(id, name))

                // when all loaded
                if (results.size == ids.size) {
                    onResult(results)
                }
            }
    }
}