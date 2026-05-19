package com.example.ekhogo.calendar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.time.LocalDate

@Composable
fun DayView(
    selectedDay: LocalDate
) {
    Text(text = "Day view for $selectedDay")
}