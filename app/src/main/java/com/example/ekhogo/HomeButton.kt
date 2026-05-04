package com.example.ekhogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ekhogo.ToDo.ToDoClass
import com.example.ekhogo.ToDo.ToDoHomePage
import com.example.ekhogo.schedule.ViewSchedule


@Composable
fun HomeButton(
    onNavigate: (Int) -> Unit,
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
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
    ) {

        Text(
            text = "  Today's ($day) Schedule",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
            //.padding(16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            {

                if (todaySchedule.isEmpty()) {
                    Text(
                        text = "No classes are scheduled for today",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    todaySchedule.forEach { item ->

                        val className = item["className"] as? String ?: ""
                        val locationName = item["locationName"] as? String ?: ""
                        val start = item["startTime"] as? String ?: ""
                        val end = item["endTime"] as? String ?: ""

                        Text(
                            text = "$className in $locationName: $start - $end",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }



        Text(
            text = "Weekly Schedule",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                if (scheduleList.isEmpty()) {
                    Text(
                        text = "No classes are scheduled for this semester",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    scheduleList.forEach { item ->

                        val className = item["className"] as? String ?: ""
                        val locationName = item["locationName"] as? String ?: ""
                        val start = item["startTime"] as? String ?: ""
                        val end = item["endTime"] as? String ?: ""
                        val days = item["days"] as? List<*> ?: emptyList<Any>()

                        Text(
                            text = "$className in $locationName: $start - $end (${
                                days.joinToString(
                                    ", "
                                )
                            })",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }


        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    onNavigate(5)
                },
                modifier = Modifier.padding(start = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Add Schedule")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "  To Do List",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {

            ToDoHomePage(ToDoList = toDoList)

        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    onNavigate(6)
                },
            ) {
                Text("Add ToDo Event")
            }
        }
    }
}