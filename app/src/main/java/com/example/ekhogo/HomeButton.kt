package com.example.ekhogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ekhogo.ToDo.ToDoClass
import com.example.ekhogo.ToDo.ToDoHomePage
import com.example.ekhogo.schedule.ViewSchedule
import com.example.ekhogo.ui.theme.EkhoGoTheme
import kotlin.collections.set


@Composable
fun HomeButton(onNavigate: (Int) -> Unit,
               toDoList: List<ToDoClass>
) {

    val scheduleList = remember { mutableStateListOf<Map<String, Any>>() }
    ViewSchedule(scheduleList)
    val day = java.time.LocalDate.now().dayOfWeek

    val dayString = when (day) {
        java.time.DayOfWeek.MONDAY -> "Mon"
        java.time.DayOfWeek.TUESDAY -> "Tue"
        java.time.DayOfWeek.WEDNESDAY -> "Wed"
        java.time.DayOfWeek.THURSDAY -> "Thu"
        java.time.DayOfWeek.FRIDAY -> "Fri"
        java.time.DayOfWeek.SATURDAY -> "Sat"
        java.time.DayOfWeek.SUNDAY -> "Sun"
    }
    val todaySchedule = scheduleList.filter { item ->
        val days = item["days"] as? List<*> ?: emptyList<Any>()
        days.contains(dayString)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End // pushes content to the right
        ) {
            Button(
                onClick = {
                    onNavigate(5)
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Add Schedule")
            }
        }
        Text(
            text = "  Today's ($day) Schedule",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        )
        {
            if (todaySchedule.isEmpty()) {
                Text(text = "  No classes are scheduled for today",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                todaySchedule.forEach { item ->

                    val className = item["className"] as? String ?: ""
                    val locationName = item["locationName"] as? String ?: ""
                    val start = item["startTime"] as? String ?: ""
                    val end = item["endTime"] as? String ?: ""

                    Text(
                        text = "  $className in $locationName: $start - $end",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }


        Text(
            text = "Weekly Schedule",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        {
        }
        scheduleList.forEach { item ->

            val className = item["className"] as? String ?: ""
            val locationName = item["locationName"] as? String ?: ""
            val start = item["startTime"] as? String ?: ""
            val end = item["endTime"] as? String ?: ""
            val days = item["days"] as? List<*> ?: emptyList<Any>()

            Text(
                text = "  $className in $locationName: $start - $end (${days.joinToString(", ")})",
                fontSize = 20.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "  To Do List",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = {
                    onNavigate(6)
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("Add ToDo Event")
            }

        }

        ToDoHomePage(ToDoList = toDoList)

    }
}

