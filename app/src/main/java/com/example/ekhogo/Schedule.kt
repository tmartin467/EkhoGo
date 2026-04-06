package com.example.ekhogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Schedule(){

    val classes = remember { mutableStateOf("") }
    Column( modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
    ) {
        OutlinedTextField(
            value = classes.value,
            onValueChange = { classes.value = it },
            label = { Text("Enter an enrolled class") },
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )
        var expanded by remember { mutableStateOf(false) }
        Box {
            Button(onClick = { expanded = true }) { Text("Select Time") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("Edit") }, onClick = { /* Handle */ })
                DropdownMenuItem(text = { Text("Delete") }, onClick = { /* Handle */ })
            }
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
        )
        {
            Text("Current Schedule placeholder")
        }
    }

}
