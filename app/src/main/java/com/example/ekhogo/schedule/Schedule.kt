package com.example.ekhogo.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun Schedule() {

    val classes = remember { mutableStateOf("") }
    val location = remember {mutableStateOf("")}
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {

        val scheduleList = remember { mutableStateListOf<Map<String, Any>>() }


        ViewSchedule(scheduleList)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "  Schedule Maker",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = classes.value,
            onValueChange = { classes.value = it },
            label = { Text("Enter enrolled class") },
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = location.value,
            onValueChange = { location.value = it },
            label = { Text("Enter class location") },
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        val startTime = listOf(
            "Start Time",
            "8 AM",
            "9 AM",
            "10 AM",
            "11 AM",
            "12 PM",
            "1 PM",
            "2 PM",
            "3 PM",
            "4 PM",
            "5 PM",
            "6 PM",
            "7 PM"
        )
        var expandedStart by remember { mutableStateOf(false) }
        var selectedStart by remember { mutableStateOf(startTime[0]) }

        val endTime = listOf(
            "End Time",
            "9 AM",
            "10 AM",
            "11 AM",
            "12 PM",
            "1 PM",
            "2 PM",
            "3 PM",
            "4 PM",
            "5 PM",
            "6 PM",
            "7 PM",
            "8 PM",
            "9 PM",
            "10 PM"
        )
        var expandedEnd by remember { mutableStateOf(false) }
        var selectedEnd by remember { mutableStateOf(endTime[0]) }

        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
        var expanded by remember { mutableStateOf(false) }
        var selectedDays by remember { mutableStateOf(setOf<String>()) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = selectedStart,
                modifier = Modifier
                    .weight(1f)
                    .clickable { expandedStart = true }
                    .padding(16.dp)
            )

            // Start time
            DropdownMenu(
                expanded = expandedStart,
                onDismissRequest = { expandedStart = false }
            ) {
                startTime.forEach { selectedOption ->
                    DropdownMenuItem(
                        text = { Text(selectedOption) },
                        onClick = {
                            selectedStart = selectedOption
                            expandedStart = false
                        }
                    )
                }
            }

            Text(
                text = selectedEnd,
                modifier = Modifier
                    .weight(1f)
                    .clickable { expandedEnd = true }
                    .padding(16.dp)
            )

            // End Time
            DropdownMenu(
                expanded = expandedEnd,
                onDismissRequest = { expandedEnd = false }
            ) {
                endTime.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            selectedEnd = selectionOption
                            expandedEnd = false
                        }
                    )
                }
            }
/*
            val displayText = if (selectedDays.isEmpty()) {
                "Select days"
            } else {
                selectedDays.joinToString(", ")
            }
*/

            Text(
                text = "Select Days",
                modifier = Modifier
                    .weight(1f)
                    .clickable { expanded = true }
                    .padding(16.dp)
                    //.background(Color.LightGray, shape = RoundedCornerShape(8.dp))

            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                daysOfWeek.forEach { day ->
                    DropdownMenuItem(
                        text = {
                            Row() {
                                Checkbox(
                                    checked = selectedDays.contains(day),
                                    onCheckedChange = { isChecked ->
                                        selectedDays = if (isChecked) {
                                            selectedDays + day
                                        } else {
                                            selectedDays - day
                                        }
                                    }
                                )
                                Text(day)
                            }
                        },
                        onClick = {
                            selectedDays = if (selectedDays.contains(day)) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)

        )
        {

            Button(onClick = {
                val user = FirebaseAuth.getInstance().currentUser
                val uid = user?.uid
                val db = FirebaseFirestore.getInstance()

                val classData = hashMapOf(
                    "className" to classes.value,
                    "locationName" to location.value,
                    "startTime" to selectedStart,
                    "endTime" to selectedEnd,
                    "days" to selectedDays.toList()
                )

                if (uid != null) {
                    db.collection("users")
                        .document(uid)
                        .collection("classes")
                        .add(classData)
                }

            },
                modifier = Modifier.padding(start = 2.dp)
            ) {
                Text("Add Class")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
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
                    modifier = Modifier.padding(vertical = 4.dp)
                )
        }
    }
    }





