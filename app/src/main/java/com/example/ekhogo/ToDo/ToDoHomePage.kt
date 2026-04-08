package com.example.ekhogo.ToDo

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
fun ToDoHomePage(ToDoList: List<ToDoClass>) {

    val todoSchedule = ToDoList

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        //Spacer(modifier = Modifier.height(8.dp))

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