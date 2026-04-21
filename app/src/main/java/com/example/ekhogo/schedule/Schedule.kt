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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Schedule() {


    var showDialogStart by remember { mutableStateOf(false) }
    var selectedTimeStart by remember { mutableStateOf("Select Start Time") }

    var showDialogEnd by remember { mutableStateOf(false) }
    var selectedTimeEnd by remember { mutableStateOf("Select End Time") }

    val classes = remember { mutableStateOf("") }
    val location = remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
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

        val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
        var expanded by remember { mutableStateOf(false) }
        var selectedDays by remember { mutableStateOf(setOf<String>()) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            TextButton(
                onClick = { showDialogStart = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.Red
                )
            ) {
                Text(
                    text = selectedTimeStart,
                    fontSize = 20.sp,
                )
            }

            var amPmStart = "AM"
            var pmHourStart = 0

            if (showDialogStart) {
                TimePickerDialogUI(
                    onConfirm = { hour, minute ->

                        if (hour >= 12) {
                            pmHourStart = hour - 12
                            amPmStart = "PM"
                        } else if (hour < 12) {
                            amPmStart = "AM"
                            pmHourStart = hour
                        }

                        selectedTimeStart =
                            String.format("%02d:%02d %s", pmHourStart, minute, amPmStart)
                        showDialogStart = false
                    },
                    onDismiss = {
                        showDialogStart = false
                    }
                )
            }

            TextButton(
                onClick = { showDialogEnd = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.Red
                )
            ) {
                Text(
                    text = selectedTimeEnd,
                    fontSize = 20.sp,
                )
            }
            var amPmEnd = "AM"
            var pmHourEnd = 0
            if (showDialogEnd) {
                TimePickerDialogUI(
                    onConfirm = { hour, minute ->
                        if (hour >= 12) {
                            pmHourEnd = hour - 12
                            amPmEnd = "PM"
                        } else if (hour < 12) {
                            amPmEnd = "AM"
                            pmHourEnd = hour
                        }

                        selectedTimeEnd = String.format("%02d:%02d %s", pmHourEnd, minute, amPmEnd)
                        showDialogEnd = false
                    },
                    onDismiss = {
                        showDialogEnd = false
                    }
                )
            }


            val selectedDaysText = if (selectedDays.isEmpty()) {
                "Select Days"
            } else {
                selectedDays.joinToString(", ")
            }

            TextButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.Red
                )
            ) {
                Text(
                    text = selectedDaysText,
                    fontSize = 20.sp,
                )
            }

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
                .padding(vertical = 4.dp),

            )
        {

            Button(
                onClick = {
                    val user = FirebaseAuth.getInstance().currentUser
                    val uid = user?.uid
                    val db = FirebaseFirestore.getInstance()


                    val classData = hashMapOf(
                        "className" to classes.value,
                        "locationName" to location.value,
                        "startTime" to selectedTimeStart,
                        "endTime" to selectedTimeEnd,
                        "days" to selectedDays.toList()
                    )

                    if (uid != null) {
                        db.collection("users")
                            .document(uid)
                            .collection("classes")
                            .add(classData)
                    }

                },
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text("Add Class")
            }
        }

        Text(
            text = "  Current Schedule",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center

        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        )
        {

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
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                }
            }
        }

    }
}



