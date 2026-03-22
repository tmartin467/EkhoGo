package com.example.ekhogo

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ekhogo.ui.theme.EkhoGoTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val selectedTab = remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { CenterAlignedTopAppBar(title = { Text("EkhoGo", fontSize = 50.sp) }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab.intValue == 0,
                    onClick = { selectedTab.intValue = 0 },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                    label = { Text("Calendar") }
                )
                NavigationBarItem(
                    selected = selectedTab.intValue == 1,
                    onClick = { selectedTab.intValue = 1 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Friends") },
                    label = { Text("Friends") }
                )
                NavigationBarItem(
                    selected = selectedTab.intValue == 2,
                    onClick = { selectedTab.intValue = 2 },
                    icon = { Icon(Icons.Default.Email, contentDescription = "Messaging") },
                    label = { Text("Messages") }
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

                // If Calendar is selected
                0 -> Text("Calendar screen")

                // If Friends is selected
                1 -> Text("Friends screen")

                // If Messages is selected
                2 -> MessagesScreen()
            }
        }
    }
}

// Represents one chat message
data class ChatMessage(
    val text: String,
    val isSentByMe: Boolean
)

// This is the screen that shows when Messages tab is selected
@Composable
fun MessagesScreen() {

    // This stores what the user types in the input box
    val messageText = remember { mutableStateOf("") }

    // The stores all the messages that have been sent
    val messages = remember { mutableStateListOf<ChatMessage>() }

    // Connects to Firebase Firestore database
    val db = FirebaseFirestore.getInstance()

    // Gets the currently logged-in Firebase user
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Gets this user's unique ID
    val currentUserId = currentUser?.uid ?: ""

    // Listen for messages from Firestore in real time
    LaunchedEffect(Unit) {
        db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->

                // Stop if Firestore gives an error
                if (error != null) {
                    return@addSnapshotListener
                }

                // Clear old messages before reloading the newest list
                messages.clear()

                // Read each Firebase document and turn it into
                // a ChatMessage
                snapshot?.documents?.forEach { document ->
                    val text = document.getString("text") ?: ""

                    // Read the sender's user ID from Firestore
                    val senderId = document.getString("senderId") ?: ""

                    messages.add(
                        ChatMessage(
                            text = text,

                            // If the senderId matches the current user, show
                            // message on the right
                            isSentByMe = senderId == currentUserId
                        )
                    )
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // TOP:
        // Show messages
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text("Messages")

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(messages) { message ->

                    // Put my messages on the right and other messages
                    // on the left
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = if (message.isSentByMe) {
                            Alignment.CenterEnd
                        } else {
                            Alignment.CenterStart
                        }
                    ) {
                        Text(
                            text = message.text,
                            modifier = Modifier
                                .background(
                                    color = if (message.isSentByMe) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                }
            }
            // BOTTOM:
            // Input field where user types message
            // Should stay at the bottom of the screen
            Column {

                OutlinedTextField(
                    value = messageText.value,
                    onValueChange = { messageText.value = it },
                    label = { Text("Type a message") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Button to send
                Button(
                    onClick = {
                        // Only send if the text box is NOT empty
                        if (messageText.value.isNotBlank()) {

                            // Create message data for Firestore
                            val messageData = hashMapOf(
                                "text" to messageText.value,
                                "senderId" to currentUserId,
                                "timestamp" to System.currentTimeMillis()
                            )

                            // Send to Firebase
                            db.collection("messages")
                                .add(messageData)
                                .addOnSuccessListener {
                                    Log.d("FIREBASE", "Message sent successfully!")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FIREBASE", "Error sending message", e)
                                }

                            // Clear the text box after sending
                            messageText.value = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Send")
                }

            }
        }
    }
}


// Preview always at the bottom for cleaner readability
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun HomeScreenPreview() {
    EkhoGoTheme {
        HomeScreen()
    }
}