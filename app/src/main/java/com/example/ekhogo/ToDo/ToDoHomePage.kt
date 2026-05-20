package com.example.ekhogo.ToDo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton


@Composable
fun ToDoHomePage(
    ToDoList: List<ToDoClass>,
    onDelete: (ToDoClass) -> Unit = {}
    ) {

    val todoSchedule = ToDoList

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        //Spacer(modifier = Modifier.height(8.dp))

        if (todoSchedule.isEmpty()) {
            Text(
                text = "No To-Do items yet",
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        } else {
            todoSchedule.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ){
                    Column(
                        modifier = Modifier.weight(1f)
                    ){
                        Text(
                            text = "${item.name}",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " ${item.time}",
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )

                        Text(
                            text = " ${item.location}",
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    IconButton(
                        onClick = { onDelete(item) }
                    ){
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete To-Do",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}