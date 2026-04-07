package com.example.ekhogo.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddScheduleScreen(
    onSave: (ClassSchedule) -> Unit
) {
    var className by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var isTodo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Create Class Schedule")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = className,
            onValueChange = { className = it },
            label = { Text("Class Name or To-Do") },
            placeholder = { Text("COMP 350/Study") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time") },
            placeholder = { Text("10:00 am") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            placeholder = { Text("SIE 2411/Library") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isTodo,
                onCheckedChange = { isTodo = it }
            )
            Text("Add as To-Do Item")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (
                    className.isNotBlank() &&
                    time.isNotBlank() &&
                    location.isNotBlank()
                ) {
                    onSave(
                        ClassSchedule(
                            name = className,
                            time = time,
                            location = location,
                            isTodo = isTodo
                        )
                    )
                }
            }
        ) {
            Text("Save Schedule")
        }
    }
}