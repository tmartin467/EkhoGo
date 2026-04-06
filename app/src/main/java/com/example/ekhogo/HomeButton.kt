package com.example.ekhogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ekhogo.ui.theme.EkhoGoTheme
import kotlin.collections.set


@Composable
fun HomeButton(onNavigate: (Int) -> Unit) {
    Column( modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        ) {
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
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
            )
            {
                Text("Today Schedule placeholder")
            }
            Card(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
            )
            {
            Text("Weekly Schedule placeholder")
            }
    }



}
