package com.example.ekhogo.calendar

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun mapGoogleColorIdToAppColor(colorId: String): String {
    return when (colorId) {
        "1" -> "lavender"
        "2" -> "sage"
        "3" -> "grape"
        "4" -> "flamingo"
        "5" -> "banana"
        "6" -> "tangerine"
        "7" -> "peacock"
        "8" -> "graphite"
        "9" -> "blueberry"
        "10" -> "basil"
        "11" -> "tomato"
        else -> "peacock"
    }
}

suspend fun fetchGoogleEventsAndSaveToFirestore(accessToken: String) {
    withContext(Dispatchers.IO) {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: return@withContext
        val db = FirebaseFirestore.getInstance()

        val url = URL(
            "https://www.googleapis.com/calendar/v3/calendars/primary/events" +
                    "?singleEvents=true&orderBy=startTime"
        )

        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer $accessToken")

        val response = conn.inputStream.bufferedReader().readText()
        val items = JSONObject(response).getJSONArray("items")

        val eventsRef = db.collection("users")
            .document(uid)
            .collection("events")

        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)

            val title = item.optString("summary", "Google Event")
            val start = item.getJSONObject("start")
            val end = item.getJSONObject("end")

            val isAllDay = start.has("date")

            val date = if (isAllDay) {
                start.getString("date")
            } else {
                OffsetDateTime.parse(start.getString("dateTime")).toLocalDate().toString()
            }

            val timeStart = if (isAllDay) "" else {
                OffsetDateTime.parse(start.getString("dateTime"))
                    .toLocalTime()
                    .format(DateTimeFormatter.ofPattern("h:mm a"))
            }

            val timeEnd = if (isAllDay) "" else {
                OffsetDateTime.parse(end.getString("dateTime"))
                    .toLocalTime()
                    .format(DateTimeFormatter.ofPattern("h:mm a"))
            }

            val googleId = item.optString("id", "")
            val googleColorId = item.optString("colorId", "")
            val color = mapGoogleColorIdToAppColor(googleColorId)

            eventsRef
                .whereEqualTo("googleEventId", googleId)
                .get()
                .addOnSuccessListener { existing ->

                    if (existing.isEmpty) {
                        val eventData = hashMapOf(
                            "title" to title,
                            "date" to date,
                            "timeStart" to timeStart,
                            "timeEnd" to timeEnd,
                            "color" to color,
                            "googleColorId" to googleColorId,
                            "isAllDay" to isAllDay,
                            "source" to "google",
                            "googleEventId" to googleId,
                            "createdAt" to System.currentTimeMillis()
                        )

                        eventsRef.add(eventData)
                    }
                }
        }
    }
}