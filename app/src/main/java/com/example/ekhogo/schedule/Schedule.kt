package com.example.ekhogo.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Schedule(scheduleList: List<ClassSchedule>) {

    val weeklySchedule = scheduleList.filter { it.isWeekly }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        // Today's Schedule
        Text(
            text = "Today's Schedule",
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (scheduleList.isEmpty()) {
            Text("No classes scheduled today")
        } else {
            scheduleList.forEach { item ->
                Text("• ${item.name} - ${item.time}")
                Text("  ${item.location}")
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekly Schedule
        Text(
            text = "Weekly Schedule",
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (weeklySchedule.isEmpty()) {
            Text("No weekly classes yet")
        } else {
            weeklySchedule.forEach { item ->
                Text("• ${item.name} - ${item.time}")
                Text("  ${item.location}")
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}