package com.example.ekhogo.schedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ViewSchedule(scheduleList: MutableList<Map<String, Any>>){

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        val db = FirebaseFirestore.getInstance()

        if (uid != null) {
            db.collection("users")
                .document(uid)
                .collection("classes")
                .addSnapshotListener { result, error ->

                    if (error != null) return@addSnapshotListener

                    if (result != null) {
                        scheduleList.clear()
                        for (doc in result) {
                            scheduleList.add(doc.data)
                        }
                    }
                }
        }
    }
}
