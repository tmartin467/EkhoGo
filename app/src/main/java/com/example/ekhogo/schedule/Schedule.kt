package com.example.ekhogo.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Schedule(scheduleList: List<ClassSchedule>) {

    val todaySchedule = scheduleList.filter { !it.isTodo }
    val todoSchedule = scheduleList.filter { it.isTodo }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        // Today's Schedule
        Text(
            text = "Today's Class Schedule",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (todaySchedule.isEmpty()) {
            Text("No classes scheduled today")
        } else {
            todaySchedule.forEach { item ->
                Text("• ${item.name} - ${item.time}")
                Text("  ${item.location}")
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekly Schedule
        Text(
            text = "To-Do Schedule",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (todoSchedule.isEmpty()) {
            Text("No To-Do items yet")
        } else {
            todoSchedule.forEach { item ->
                Text("• ${item.name} - ${item.time}")
                Text("   ${item.location}")
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}