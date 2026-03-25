package com.example.ekhogo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme

@Composable
fun CalendarScreen() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }


    var currentWeekStart by remember {
        mutableStateOf(
            selectedDate.minusDays(selectedDate.dayOfWeek.value.toLong() - 1)
        )
    }

    val events = remember {
        mutableStateMapOf<LocalDate, List<String>>()
    }

    var eventText by remember { mutableStateOf("") }


    Column( modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { currentWeekStart = currentWeekStart.minusWeeks(1) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = androidx.compose.ui.graphics.Color.Black
                ),
                elevation = null
            ) {
                Text("<<--")
            }

            Text(
                text = "${currentWeekStart.month.getDisplayName(
                    java.time.format.TextStyle.FULL,
                    java.util.Locale.getDefault()
                )} ${currentWeekStart.year}", fontSize = 30.sp,
                modifier = Modifier.padding(top = 6.dp)


            )

            Button(
                onClick = { currentWeekStart = currentWeekStart.plusWeeks(1) },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    contentColor = androidx.compose.ui.graphics.Color.Black
                ),
                elevation = null
            ) {
                Text("-->>")
            }
            }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0..6) {
                val date = currentWeekStart.plusDays(i.toLong())
                val isSelected = date == selectedDate
                val dayEvent = events[date]

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { selectedDate = date }
                        .padding(8.dp)
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            color = if (isSelected) Color.White else Color.Black
                        )
                    }

                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        val formatter = DateTimeFormatter.ofPattern("EEE, MMMM d")
        Text(
            text = "Events for ${selectedDate.format(formatter)}",
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 25.sp
        )

        val selectedEvents = events[selectedDate]

        if (selectedEvents != null) {
            for ((index, event) in selectedEvents.withIndex()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = event,
                        modifier = Modifier.weight(1f),
                        fontSize = 20.sp
                    )
                    Button(
                        onClick = {
                            val newList = selectedEvents.toMutableList()
                            newList.removeAt(index)
                            events[selectedDate] = newList
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = eventText,
            onValueChange = { eventText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("") }
        )

        Button(
            onClick = {
                if (eventText != "") {

                    val currentList = events[selectedDate]

                    val newList = mutableListOf<String>()

                    if (currentList != null) {
                        for (event in currentList) {
                            newList.add(event)
                        }
                    }

                    newList.add(eventText)

                    events[selectedDate] = newList

                    eventText = ""
                }
            },
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text("Add Event")
        }
    }
}