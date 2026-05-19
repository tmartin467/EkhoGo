package com.example.ekhogo.message

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// This is the screen that shows when Messages tab is selected
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(viewModel: MessagesViewModel) {

    // This stores what the user types in the input box
    val messageText = remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()

    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val showUnsend = remember { mutableStateOf(false) }
    val showNewChat = remember { mutableStateOf(false) }
    val showParticpants =  remember { mutableStateOf(false) }




    // The stores all the messages that have been sent
    val messages by viewModel.messages.collectAsState()

    // List of conversation previews (inbox-style chat list)
    val conversationPreviews by viewModel.conversationPreviews.collectAsState()

    val selectMessage = remember { mutableStateOf<ChatMessage?>(null) }


    val isInConversation by viewModel.isInConversation.collectAsState()

    // Listen for messages from Firestore in real time
    DisposableEffect(Unit) {
        viewModel.onMessagesScreenOpened()
        viewModel.loadConversationsPreview()

        onDispose {
            viewModel.onMessagesScreenClosed()
        }
    }



    var userName by remember { mutableStateOf("") }
    var selectedUsers by remember { mutableStateOf(setOf<String>()) }
    var userSearch by remember { mutableStateOf("") }
    var friendUsers by remember { mutableStateOf<List<FriendUser>>(emptyList()) }

    LaunchedEffect(Unit) {

        loadingFriends(uid) { ids ->

            loadingNames(ids) { users ->

                friendUsers = users
                //val name = users
            }
        }
    }

    LaunchedEffect(Unit){
        loadingOwnName(uid){name ->
            userName = name
        }
    }

    val filterName = friendUsers.filter {
        it.name.contains(userSearch, ignoreCase = true)
    }


    Scaffold(
        floatingActionButton = {
            if(!isInConversation) {
                FloatingActionButton(
                    onClick = { showNewChat.value = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(2.dp)
        )
        {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {

                // TOP:
                // Show messages
                Column(
                    modifier = Modifier.weight(1f)
                ) {



                    if (!isInConversation) {
                        Text("Messages")
                        // INBOX VIEW
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            items(
                                items = conversationPreviews.filter { !it.deletedFor.contains(uid) },
                                key = { it.conversationId }
                            ) { preview ->

                                val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        if (value == SwipeToDismissBoxValue.EndToStart) {
                                            deleteMessageThread(preview.conversationId)
                                            true
                                        } else {
                                            false
                                        }
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
                                        ) {
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
                                                    viewModel.selectOtherUser(preview.otherUserName)
                                                    viewModel.openConversation(preview.conversationId, preview.isGroup)
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

                        val titleName by viewModel.selectedOtherUser.collectAsState()
                        val participants by viewModel.participants.collectAsState()
                        val isGroup by viewModel.isGroupChat.collectAsState()

                        val listState = rememberLazyListState()

                        Text(
                            text = titleName,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable(enabled = isGroup) {
                                    showParticpants.value = true
                                    viewModel.loadParticipants()
                                }
                                .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = RoundedCornerShape(12.dp)
                            )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center

                        )

                        LaunchedEffect(messages.size) {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.lastIndex)
                            }
                        }

                        if(showParticpants.value) {
                                Dialog(onDismissRequest = { showParticpants.value = false }) {

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(16.dp))
                                            .padding(16.dp),
                                        contentAlignment = Alignment.TopCenter

                                    ) {

                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                        ) {
                                            Text(
                                                "Members",
                                                fontSize = 24.sp,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(24.dp))

                                            participants
                                                .sorted()
                                                .forEach { participant ->

                                                Text(
                                                    text = participant,
                                                    fontSize = 18.sp,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(24.dp))


                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.leaveChat()
                                                        showParticpants.value = false
                                                    },
                                                ) {
                                                    Text("Delete and Block Chat")
                                                }
                                            }

                                        }
                                    }
                                }

                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            itemsIndexed(messages) { index, message ->

                                val prev = messages.getOrNull(index - 1)

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {

                                    val group by viewModel.isGroupChat.collectAsState()
                                    if(!group) {
                                        if ((prev?.isSentByMe == true || prev == null) && (message.isSentByMe == false)) {

                                            val user by viewModel.selectedOtherUser.collectAsState()
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = user,
                                                fontSize = 14.sp,
                                            )

                                        }
                                    }
                                    else{
                                        val prevSender = prev?.senderId

                                        val showName =
                                            prevSender == null || prevSender != message.senderId

                                        if (showName && !message.isSentByMe) {

                                            var name by remember { mutableStateOf("...") }

                                            LaunchedEffect(message.senderId) {
                                                db.collection("users")
                                                    .document(message.senderId)
                                                    .get()
                                                    .addOnSuccessListener { doc ->
                                                        name = doc.getString("name") ?: "Unknown"
                                                    }
                                            }
                                            Text(
                                                text = name,
                                                fontSize = 14.sp
                                            )
                                        }

                                    }


                                    Text(
                                        text = message.text,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .align(
                                                if (message.isSentByMe)
                                                    Alignment.End
                                                else
                                                    Alignment.Start
                                            )
                                            .background(
                                                color = if (message.isSentByMe)
                                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.7f
                                                    )
                                                else
                                                    MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(12.dp)
                                            .combinedClickable(
                                                enabled = (message.isSentByMe) && (!message.text.endsWith(
                                                    "unsent a message"
                                                )),
                                                onClick = {},
                                                onLongClick = {
                                                    selectMessage.value = message
                                                    showUnsend.value = true
                                                }
                                            )
                                    )
                                }

                            }
                        }
                    }


                    if (showUnsend.value) {
                        AlertDialog(
                            onDismissRequest = { showUnsend.value = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    showUnsend.value = false
                                    selectMessage.value?.let { message ->
                                        viewModel.unsendMessage(message.id)
                                    }
                                }) {
                                    Text("Unsend")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showUnsend.value = false }) {
                                    Text("Cancel")
                                }
                            },
                            title = { Text("Unsend Message") },
                            text = { Text("Are you sure you want to unsend message?") }
                        )
                    }

                }

                var chatName by remember { mutableStateOf("") }

                if (showNewChat.value) {
                    Dialog(onDismissRequest = { showNewChat.value = false }) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {

                            Column {

                                Text("New Chat")

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = chatName,
                                    onValueChange = { chatName = it },
                                    label = { Text("Chat name") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = userSearch,
                                    onValueChange = { userSearch = it },
                                    label = { Text("Search users") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                ) {
                                    items(filterName) { user ->

                                        val isSelected = user.id in selectedUsers

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        Color.Gray.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .background(
                                                    if (isSelected)
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                    else
                                                        Color.Transparent,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    selectedUsers = if (isSelected) {
                                                        selectedUsers - user.id
                                                    } else {
                                                        selectedUsers + user.id
                                                    }
                                                }
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = user.name,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))



                                val friendsList = selectedUsers.joinToString(", ") { id ->
                                    friendUsers.find { it.id == id }?.name ?: ""}

                                val fullList = userName + ", " + selectedUsers.joinToString(", ") { id ->
                                    friendUsers.find { it.id == id }?.name ?: ""
                                }


                                Text(friendsList)


                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        val actualName =
                                            if (chatName.isBlank()) {
                                                fullList
                                            } else {
                                                chatName
                                            }

                                        if (selectedUsers.size == 1) {

                                            val otherUserId = selectedUsers.first()

                                            viewModel.startDirectConversation(otherUserId) { conversationId ->
                                                viewModel.selectOtherUser(
                                                    friendUsers.find { it.id == otherUserId }?.name ?: ""
                                                )
                                                viewModel.openConversation(conversationId, false)
                                            }

                                        } else {
                                            viewModel.createGroupConversation(
                                                groupName = actualName,
                                                selectedUserIds = selectedUsers.toList()
                                            ) { success ->
                                                if (success) showNewChat.value = false
                                            }
                                        }
                                        showNewChat.value = false
                                        chatName = ""
                                        selectedUsers = emptySet()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Create Chat")
                                }

                                TextButton(
                                    onClick = { showNewChat.value = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cancel")
                                }
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
    }