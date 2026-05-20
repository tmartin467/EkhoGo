package com.example.ekhogo

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ekhogo.ToDo.ToDoClass
import com.example.ekhogo.ToDo.ToDoHomePage
import com.example.ekhogo.ToDo.ToDoScreen
import com.example.ekhogo.calendar.CalendarScreen
import com.example.ekhogo.friends.FriendsScreen
import com.example.ekhogo.map.CampusMapScreen
import com.example.ekhogo.message.MessagesScreen
import com.example.ekhogo.message.MessagesViewModel
import com.example.ekhogo.schedule.Schedule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAccountLogout: () -> Unit,
    toProfileScreen: () -> Unit,
    isDarkMode: Boolean,
    themeMode: String,
    onThemeModeChange: (String) -> Unit
) {
    val selectedTab = remember { mutableIntStateOf(0) }
    var openSharedClassmatesFilter by remember { mutableStateOf(false) }
    val messagesViewModel: MessagesViewModel = viewModel()
    val ToDoList = remember { mutableStateListOf<ToDoClass>() }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    var profileImageUrl by remember { mutableStateOf("") }
    val unreadCount by messagesViewModel.unreadCount.collectAsState()
    var expandedMenu by remember { mutableStateOf(false) } // Variable that tracks whether the dropdown menu is open or not
    val navigationItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,
        selectedTextColor = Color.White,
        unselectedIconColor = Color.White.copy(alpha = 0.75f),
        unselectedTextColor = Color.White.copy(alpha = 0.75f),
        indicatorColor = MaterialTheme.colorScheme.primaryContainer
    )

    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    profileImageUrl = document.getString("profileImageUrl") ?: ""
                }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.height(84.dp),
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = 32.dp, y = (-2).dp)
                    ) {
                        Text(
                            text = "EkhoGo",
                            fontSize = 34.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )

                        Image(
                            painter = painterResource(id = R.drawable.homepage_icon),
                            contentDescription = "EkhoGo logo",
                            modifier = Modifier
                                .size(75.dp)
                                .align(Alignment.Center)
                                .offset(x = (-85).dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                },
                actions = {
                    Box {
                        // Profile Icon on the top right of the screen
                        IconButton(
                            onClick = { expandedMenu = true },
                            modifier = Modifier.offset(y = (-6).dp)
                        ) {
                            if (profileImageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = profileImageUrl,
                                    contentDescription = "Profile Menu",
                                    modifier = Modifier
                                        .size(35.dp)
                                        .border(1.dp, Color.White, CircleShape)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile menu",
                                    tint = Color.White
                                )
                            }
                        }
                        // Dropdown menu for the profile icon
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            // Placeholder for when editing profile is implemented
                            DropdownMenuItem(
                                text = { Text("View Profile") },
                                onClick = {
                                    expandedMenu = false
                                    toProfileScreen()
                                }
                            )
                            // Logout button and returns to the login screen
                            DropdownMenuItem(
                                text = { Text("Log Out") },
                                onClick = {
                                    expandedMenu = false
                                    onAccountLogout()
                                }
                            )
                            Text(
                                text = "Theme",
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 8.dp,
                                    bottom = 4.dp
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )

                            DropdownMenuItem(
                                text = { Text("System Default") },
                                onClick = {
                                    expandedMenu = false
                                    onThemeModeChange("system")
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Light Mode") },
                                onClick = {
                                    expandedMenu = false
                                    onThemeModeChange("light")
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Dark Mode") },
                                onClick = {
                                    expandedMenu = false
                                    onThemeModeChange("dark")
                                }
                            )

                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                NavigationBarItem(
                    selected = selectedTab.intValue == 0,
                    onClick = { selectedTab.intValue = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = navigationItemColors
                )
                NavigationBarItem(
                    selected = selectedTab.intValue == 1,
                    onClick = { selectedTab.intValue = 1 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                    label = { Text("Calendar") },
                    colors = navigationItemColors
                )
                NavigationBarItem(
                    selected = selectedTab.intValue == 2,
                    onClick = {
                        openSharedClassmatesFilter = false
                        selectedTab.intValue = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Friends") },
                    label = { Text("Friends") },
                    colors = navigationItemColors
                )
                NavigationBarItem(
                    selected = selectedTab.intValue == 3,
                    onClick = { selectedTab.intValue = 3 },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Map") },
                    label = { Text("Map") },
                    colors = navigationItemColors
                )
                NavigationBarItem(
                    selected = selectedTab.intValue == 4,
                    onClick = { selectedTab.intValue = 4 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFFB3261E)
                                    ) {
                                        Text(
                                            text = if (unreadCount > 99) "99+" else unreadCount.toString()
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Messages")
                        }
                    },
                    label = { Text("Messages") },
                    colors = navigationItemColors
                )
            }
        }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            // Check which tab is selected
            when (selectedTab.intValue) {

                // If Home button is selected
                //0 -> HomeButton(onNavigate = { selectedTab.intValue = it })
                0 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .padding(top = 8.dp)
                ) {
                    HomeButton(
                        onNavigate = { tab ->
                            if (tab == 2){
                                openSharedClassmatesFilter = true
                            }
                            selectedTab.intValue = tab },
                        toDoList = ToDoList
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ToDoHomePage(ToDoList = ToDoList)
                }

                // If Calendar is selected
                1 -> CalendarScreen()

                // If Friends is selected
                2 -> FriendsScreen(
                    viewModel = messagesViewModel,
                    onNavigateToMessages = {
                        selectedTab.intValue = 4
                    },
                    showSharedClassmatesOnly = openSharedClassmatesFilter
                )



                // If Maps is selected
                3 -> CampusMapScreen(isDarkMode = isDarkMode)

                // If Messages is selected
                4 -> MessagesScreen(viewModel = messagesViewModel,)

                // If Schedule button on homescreen is selected
                5 -> Schedule()

                // If ToDo button on homescreen is selected
                6 -> ToDoScreen(onSave = { newSchedule ->
                    ToDoList.add(newSchedule)
                    selectedTab.intValue = 0
                }
                )
            }
        }
    }
}

// Preview always at the bottom for cleaner readability
/*
@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
fun HomeScreenPreview() {
EkhoGoTheme {
    HomeScreen(onAccountLogout = {}, toProfileScreen = {})
}
}
*/