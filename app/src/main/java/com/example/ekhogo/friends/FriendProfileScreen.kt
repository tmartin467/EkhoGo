package com.example.ekhogo.friends

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ekhogo.profile.ProfileCard
import com.example.ekhogo.profile.ProfileInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.example.ekhogo.message.MessagesViewModel
import androidx.compose.material.icons.filled.Email
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(
    friendId: String,
    onBack: () -> Unit,
    viewModel: MessagesViewModel,
    onNavigateToMessages: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") }
    var classesList by remember { mutableStateOf(listOf<String>()) }
    var interestsList by remember { mutableStateOf(listOf<String>()) }
    var loadingProfile by remember { mutableStateOf(true) }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(friendId) {
        db.collection("users")
            .document(friendId)
            .get()
            .addOnSuccessListener { document ->
                name = document.getString("name") ?: ""
                major = document.getString("major") ?: ""
                bio = document.getString("bio") ?: ""
                profileImageUrl = document.getString("profileImageUrl") ?: ""

                classesList = (document.get("classes") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: emptyList()

                interestsList = (document.get("interests") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?: emptyList()

                loadingProfile = false
            }
            .addOnFailureListener {
                loadingProfile = false
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        if (!loadingProfile) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (profileImageUrl.isNotBlank()) {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = "Friend profile picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default profile picture",
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = name.ifBlank { "Name not added yet" },
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.openConversation(friendId)
                        onNavigateToMessages()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Message",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Message")
                }

                Spacer(modifier = Modifier.height(24.dp))

                ProfileCard(title = "Personal Info") {
                    ProfileInfo(label = "Name", value = name.ifBlank { "Not added yet" })

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileInfo(label = "Major", value = major.ifBlank { "Not added yet" })

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileInfo(
                        label = "Classes",
                        value = if (classesList.isNotEmpty()) {
                            classesList.joinToString(", ")
                        } else {
                            "Not added yet"
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileInfo(
                        label = "Interests",
                        value = if (interestsList.isNotEmpty()) {
                            interestsList.joinToString(", ")
                        } else {
                            "Not added yet"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                ProfileCard(title = "About") {
                    Text(
                        text = bio.ifBlank { "No bio added yet." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}