package com.example.ekhogo.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Email
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.border
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Represents the different tabs in the Friends screen that's being shown
enum class FriendsTab {
    FRIENDS,
    REQUESTS,
    ADD_FRIENDS
}

@Composable
fun FriendsScreen(
    viewModel: MessagesViewModel,
    onNavigateToMessages: () -> Unit,
    showSharedClassmatesOnly: Boolean = false
) {

    var classmates by remember { mutableStateOf<List<Friend>>(emptyList()) }
    // selectedTab controls which friend category is displayed
    var selectedTab by remember { mutableStateOf(
            if (showSharedClassmatesOnly) {
                FriendsTab.ADD_FRIENDS
            } else {
                FriendsTab.FRIENDS
            }
        )
    }
    // searchText is used when clicking on the add friend tab to search for a user
    var searchText by remember { mutableStateOf("") }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    var currentUserClasses by remember { mutableStateOf<List<String>>(emptyList()) }

    var selectedFriendId by remember { mutableStateOf<String?>(null) }

    // Connecting to the database
    val repository = remember { FriendsRepository() }

    fun refreshClassmates() {
        repository.loadUsers { users ->
            classmates = users
        }
    }

    LaunchedEffect(Unit) {
        val uid = currentUser?.uid
        if (uid != null) {
            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    currentUserClasses = (document.get("classes") as? List<*>)
                        ?.filterIsInstance<String>()
                        ?: emptyList()
                }
        }
        refreshClassmates()
    }

    if (selectedFriendId != null) {
        FriendProfileScreen(
            friendId = selectedFriendId!!,
            onBack = { selectedFriendId = null },
            viewModel = viewModel,
            onNavigateToMessages = onNavigateToMessages
        )
        return
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

                val matchesSearch = currentFriend.name.contains(searchText, ignoreCase = true) ||
                        currentFriend.classesList.any { className ->
                            className.contains(searchText, ignoreCase = true)
                        }
                val matchesSharedClasses = !showSharedClassmatesOnly ||
                        currentFriend.classesList.any { className ->
                            currentUserClasses.any { userClass ->
                                userClass.equals(className, ignoreCase = true)
                            }
                        }
                matchesSearch && matchesSharedClasses
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
                items(items = displayedClassmates, key = { friend -> friend.id }) { friend ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                selectedFriendId = friend.id
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (friend.profileImageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = friend.profileImageUrl,
                                    contentDescription = "${friend.name} profile picture",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default profile picture",
                                    modifier = Modifier.size(56.dp)
                                )
                            }

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

                                if (friend.bio.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = friend.bio,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
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
                                            viewModel.startDirectConversation(friend.id) { conversationId ->
                                                viewModel.openConversation(conversationId, false)
                                                onNavigateToMessages()
                                            }
                                        },
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Message",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
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