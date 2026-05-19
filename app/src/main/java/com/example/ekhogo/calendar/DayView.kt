package com.example.ekhogo.calendar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.time.LocalDate

@Composable
fun DayView(
    selectedDay: LocalDate,
    events: Map<LocalDate, List<Event>>
) {
    val dayEvents = events[selectedDay] ?: emptyList()

    Text(text = "Day view for $selectedDay")
    Text(text = "Events: ${dayEvents.size}")
}