package com.example.ekhogo.message

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// This is the screen that shows when Messages tab is selected
@Composable
fun MessagesScreen(viewModel: MessagesViewModel) {

    // This stores what the user types in the input box
    val messageText = remember { mutableStateOf("") }

    // The stores all the messages that have been sent
    val messages by viewModel.messages.collectAsState()

    // List of conversation previews (inbox-style chat list)
    val conversationPreviews by viewModel.conversationPreviews.collectAsState()

    val isInConversation by viewModel.isInConversation.collectAsState()

    // Listen for messages from Firestore in real time
    DisposableEffect(Unit) {
        viewModel.onMessagesScreenOpened()
        viewModel.loadConversationsPreview()

        onDispose {
            viewModel.onMessagesScreenClosed()
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


            if (!isInConversation) {

                // 📥 INBOX VIEW
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(conversationPreviews) { preview ->

                        val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart){
                                    deleteMessageThread(preview.otherUserId)
                                }
                                it != SwipeToDismissBoxValue.StartToEnd
                            }
                        )




                        SwipeToDismissBox(
                            state = swipeToDismissBoxState,
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,

                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.CenterEnd
                                )  {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Icon",
                                        tint = Color.Red
                                    )
                                }
                            },

                            content = {
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .clickable {
                                            viewModel.openConversation(preview.otherUserId)
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = preview.otherUserName,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = preview.lastMessage,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        )
                    }
                }

            } else {

                // CHAT VIEW
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(messages) { message ->

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
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
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
            }
            // BOTTOM:
            // Input field where user types message
            // Should stay at the bottom of the screen
            if (isInConversation) {
                Column {

                    OutlinedTextField(
                        value = messageText.value,
                        onValueChange = { messageText.value = it },
                        label = { Text("Type a message") },
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.closeConversation()
                            }
                        ) {
                            Text("Back")
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (messageText.value.isNotBlank()) {
                                    viewModel.sendMessage(messageText.value)
                                    messageText.value = ""
                                }
                            },
                        ) {
                            Text("Send")
                        }

                    }
                }
            }
        }
    }
}