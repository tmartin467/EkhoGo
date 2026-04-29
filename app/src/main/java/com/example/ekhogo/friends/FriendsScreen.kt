package com.example.ekhogo.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ekhogo.message.MessagesViewModel

// Represents the different tabs in the Friends screen that's being shown
enum class FriendsTab {
    FRIENDS,
    REQUESTS,
    ADD_FRIENDS
}

@Composable
fun FriendsScreen(
    viewModel: MessagesViewModel,
    onNavigateToMessages: () -> Unit
) {

    var classmates by remember { mutableStateOf<List<Friend>>(emptyList()) }
    // selectedTab controls which friend category is displayed
    var selectedTab by remember { mutableStateOf(FriendsTab.FRIENDS) }
    // searchText is used when clicking on the add friend tab to search for a user
    var searchText by remember { mutableStateOf("") }

    // Connecting to the database
    val repository = remember { FriendsRepository() }

    fun refreshClassmates() {
        repository.loadUsers { users ->
            classmates = users
        }
    }

    LaunchedEffect(Unit) {
        refreshClassmates()
    }

    // filter the full classmate list based on the selected tab
    val visibleClassmates = when (selectedTab) {
        FriendsTab.FRIENDS -> classmates.filter { currentFriend ->
            currentFriend.status == FriendStatus.FRIENDS
        }

        FriendsTab.REQUESTS -> classmates.filter { currentFriend ->
            currentFriend.status == FriendStatus.REQUEST_RECEIVED ||
                    currentFriend.status == FriendStatus.REQUEST_SENT
        }

        FriendsTab.ADD_FRIENDS -> classmates.filter { currentFriend ->
            currentFriend.status == FriendStatus.NONE
        }
    }

    // search filtering inside the add friends tab
    val displayedClassmates =
        if (selectedTab == FriendsTab.ADD_FRIENDS) {
            visibleClassmates.filter { currentFriend ->
                currentFriend.name.contains(searchText, ignoreCase = true)
            }
        } else {
            visibleClassmates
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Friends",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Top tab buttons for switching between Friends, Requests, and Add Friends
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedTab == FriendsTab.FRIENDS) {
                Button(onClick = {
                    selectedTab = FriendsTab.FRIENDS
                    searchText = ""
                }) {
                    Text("Friends")
                }
            } else {
                OutlinedButton(onClick = {
                    selectedTab = FriendsTab.FRIENDS
                    searchText = ""
                }) {
                    Text("Friends")
                }
            }

            if (selectedTab == FriendsTab.REQUESTS) {
                Button(onClick = {
                    selectedTab = FriendsTab.REQUESTS
                    searchText = ""
                }) {
                    Text("Requests")
                }
            } else {
                OutlinedButton(onClick = {
                    selectedTab = FriendsTab.REQUESTS
                    searchText = ""
                }) {
                    Text("Requests")
                }
            }

            if (selectedTab == FriendsTab.ADD_FRIENDS) {
                Button(onClick = { selectedTab = FriendsTab.ADD_FRIENDS }) {
                    Text("Add Friends")
                }
            } else {
                OutlinedButton(onClick = { selectedTab = FriendsTab.ADD_FRIENDS }) {
                    Text("Add Friends")
                }
            }
        }

        // Search bar for add friends tab
        if (selectedTab == FriendsTab.ADD_FRIENDS) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search classmates") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display messages when clicking on the tab and there are no users there
        if (displayedClassmates.isEmpty()) {
            Text(
                text = when (selectedTab) {
                    FriendsTab.FRIENDS -> "No current friends yet."
                    FriendsTab.REQUESTS -> "No friend requests right now."
                    FriendsTab.ADD_FRIENDS -> "No classmates found."
                },
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(displayedClassmates) { friend ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = friend.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = friend.major,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Column {
                                Button(
                                    onClick = {
                                            when (friend.status) {
                                                FriendStatus.NONE -> {
                                                    repository.sendFriendRequest(friend.id) { success ->
                                                        if (success) {
                                                            refreshClassmates()
                                                        }
                                                    }
                                                }

                                                FriendStatus.REQUEST_RECEIVED -> {
                                                    repository.acceptFriendRequest(friend.id) { success ->
                                                        if (success) {
                                                            refreshClassmates()
                                                        }
                                                    }
                                                }

                                                FriendStatus.REQUEST_SENT -> Unit

                                                FriendStatus.FRIENDS -> {
                                                    repository.removeFriend(friend.id)  { success ->
                                                        if (success) {
                                                            refreshClassmates()
                                                        }
                                                    }
                                                }
                                            }
                                    },
                                    // Button is only interactable when an action can happen
                                    enabled = friend.status != FriendStatus.REQUEST_SENT
                                ) {
                                    Text(
                                        // Update button text based on the current relationship status
                                        when (friend.status) {
                                            FriendStatus.NONE -> "Add"
                                            FriendStatus.REQUEST_SENT -> "Pending"
                                            FriendStatus.REQUEST_RECEIVED -> "Accept"
                                            FriendStatus.FRIENDS -> "Unfriend"
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (friend.status == FriendStatus.REQUEST_RECEIVED) {
                                    OutlinedButton(
                                        onClick = {
                                            repository.rejectFriendRequest(friend.id) { success ->
                                                if (success) {
                                                    refreshClassmates()
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Decline")
                                    }
                                } else if (friend.status == FriendStatus.FRIENDS) {
                                    Button(
                                        onClick = {
                                            viewModel.openConversation(friend.id)
                                            onNavigateToMessages()
                                        }
                                    ) {
                                        Text("Message")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}