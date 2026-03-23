package com.example.ekhogo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.sizeIn
import com.example.ekhogo.ui.theme.EkhoGoTheme
import com.example.ekhogo.friends.FriendsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen( modifier: Modifier = Modifier) {
    val selectedTab = remember { mutableIntStateOf(0) }

    Scaffold(modifier = modifier.fillMaxSize(),
        topBar = { CenterAlignedTopAppBar(title = {Text("EkhoGo", fontSize = 50.sp)})},
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab.intValue == 0,
                    onClick = { selectedTab.intValue = 0 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar")},
                    label = { Text("Calendar")}
                )
                NavigationBarItem(
                    selected = selectedTab.intValue == 1,
                    onClick = { selectedTab.intValue = 1},
                    icon = { Icon(Icons.Default.Person, contentDescription = "Friends")},
                    label = { Text("Friends")}
                )
                NavigationBarItem(
                    selected = selectedTab.intValue == 2,
                    onClick = { selectedTab.intValue = 2},
                    icon = { Icon(Icons.Default.Email, contentDescription = "Messaging")},
                    label = { Text("Profile")}
                )
            }
        }) {
        innerPadding->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab.intValue) {
                0 -> {
                    Text("Map will go here")
                }
                1 -> {
                    FriendsScreen()
                }
                2 -> {
                    Text("Messaging/Profile coming soon")
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun HomeScreenPreview() {
    EkhoGoTheme {
        HomeScreen()
    }
}
