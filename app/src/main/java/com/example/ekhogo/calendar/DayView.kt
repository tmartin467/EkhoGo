package com.example.ekhogo.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DayView(
    selectedDay: LocalDate,
    events: Map<LocalDate, List<Event>>
) {

    val formatter = DateTimeFormatter.ofPattern("d EEEE")

    val dayEvents = events.values.flatten().filter { event ->
        val start = LocalDate.parse(event.startDate.ifBlank { event.date })
        val end = LocalDate.parse(event.endDate.ifBlank { event.date })

        selectedDay >= start && selectedDay <= end
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 120.dp)
    ) {
        Text(
            text = selectedDay.format(formatter),
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        for (hour in 0..23) {
            val displayHour = when {
                hour == 0 -> "12 AM"
                hour < 12 -> "$hour AM"
                hour == 12 -> "12 PM"
                else -> "${hour - 12} PM"
            }

            val eventsThisHour = dayEvents.filter { event ->
                val startHour = when {
                    event.isAllDay -> 0
                    event.timeStart.contains("AM") || event.timeStart.contains("PM") -> {
                        val hourPart = event.timeStart.substringBefore(":").toIntOrNull() ?: -1
                        when {
                            event.timeStart.contains("AM") && hourPart == 12 -> 0
                            event.timeStart.contains("PM") && hourPart != 12 -> hourPart + 12
                            else -> hourPart
                        }
                    }

                    else -> event.timeStart.substringBefore(":").toIntOrNull() ?: -1
                }

                startHour == hour
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = displayHour,
                    fontSize = 16.sp
                )

                Column(
                    modifier = Modifier.padding(start = 24.dp)
                ) {
                    eventsThisHour.forEach { event ->
                        Text(
                            text = event.title,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider()
        }
    }
}
