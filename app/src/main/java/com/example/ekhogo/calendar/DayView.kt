package com.example.ekhogo.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DayView(
    selectedDay: LocalDate,
    events: Map<LocalDate, List<Event>>,
    onEventClick: (Event) -> Unit
) {

    fun timeToMinutes(time: String): Int {
        val hourPart = time.substringBefore(":").toIntOrNull() ?: 0

        val minutePart =
            time.substringAfter(":")
                .substringBefore(" ")
                .toIntOrNull() ?: 0

        val isPm = time.contains("PM")
        val isAm = time.contains("AM")

        val hour24 = when {
            isAm && hourPart == 12 -> 0
            isPm && hourPart != 12 -> hourPart + 12
            else -> hourPart
        }

        return hour24 * 60 + minutePart
    }

    val formatter = DateTimeFormatter.ofPattern("d EEEE")
    val isToday = selectedDay == LocalDate.now()

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isToday) Color(0xFFFFCDD2)
                    else Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = selectedDay.format(formatter),
                fontSize = 20.sp,
                color = if (isToday) Color(0xFF4A1F1F)
                else MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (dayEvents.isEmpty()) {
            Text(
                text = "No events for this day",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            dayEvents
                .sortedBy { timeToMinutes(it.timeStart) }
                .forEach { event ->
                    Text(
                        text = "${event.timeStart} - ${event.timeEnd}\n${event.title}",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .clickable { onEventClick(event) }
                            .background(
                                color = getEventColor(event.color),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                }
        }
    }
}
