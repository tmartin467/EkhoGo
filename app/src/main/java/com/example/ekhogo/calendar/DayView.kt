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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = displayHour,
                    fontSize = 16.sp
                )
            }

            HorizontalDivider()
        }
    }
}
