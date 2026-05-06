package com.example.ekhogo.calendar

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

fun eventsFireBase(onResult: (Map<LocalDate, List<Event>>) -> Unit) {

    val user = FirebaseAuth.getInstance().currentUser
    val uid = user?.uid
    val db = FirebaseFirestore.getInstance()

    if (uid == null) return
    db.collection("users")
        .document(uid)
        .collection("events")
        .get()
        .addOnSuccessListener { result ->

            val map = mutableMapOf<LocalDate, MutableList<Event>>()

            for (doc in result) {

                val event = Event(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    date = doc.getString("date") ?: continue,
                    timeStart = doc.getString("timeStart") ?: "",
                    timeEnd = doc.getString("timeEnd") ?: "",
                    color = doc.getString("color") ?: "red",
                    isAllDay = doc.getBoolean("isAllDay") ?: false,
                    location = doc.getString("location") ?: "",
                    notes = doc.getString("notes") ?: "",
                    startDate = doc.getString("startDate") ?: doc.getString("date") ?: "",
                    endDate = doc.getString("endDate") ?: doc.getString("date") ?: ""
                )

                val date = LocalDate.parse(event.date)

                val list = map.getOrPut(date) { mutableListOf() }
                list.add(event)
            }

            onResult(map)
        }
}