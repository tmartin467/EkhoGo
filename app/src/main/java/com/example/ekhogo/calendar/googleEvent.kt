package com.example.ekhogo.calendar

import android.content.Context
import java.time.LocalDate
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

suspend fun googleEvent(
    context: Context,
    account: GoogleSignInAccount,
    title: String,
    date: LocalDate,
    timeStart: String,
    timeEnd: String,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int
) {
    val token = getAccessToken(context, account) ?: return

    val startDateTime = String.format(
        "%sT%02d:%02d:00",
        date,
        startHour,
        startMinute
    )

    val endDateTime = String.format(
        "%sT%02d:%02d:00",
        date,
        endHour,
        endMinute
    )




    val json = """
        {
          "summary": "$title",
          "start": {
            "dateTime": "$startDateTime",
            "timeZone": "America/Los_Angeles"
          },
          "end": {
            "dateTime": "$endDateTime",
            "timeZone": "America/Los_Angeles"
          }
        }
    """.trimIndent()

    val url = URL("https://www.googleapis.com/calendar/v3/calendars/primary/events")

    withContext(Dispatchers.IO) {
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $token")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        connection.outputStream.use {
            it.write(json.toByteArray())
        }

        val responseCode = connection.responseCode
        println("Google Calendar response: $responseCode")
    }
}