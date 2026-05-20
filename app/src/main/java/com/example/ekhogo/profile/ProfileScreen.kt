package com.example.ekhogo.profile

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import coil.compose.AsyncImage
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.unit.dp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(toHomeScreen: () -> Unit) {
    // variables for storing editable user data
    val currentUser =
        FirebaseAuth.getInstance().currentUser // Firebase Instance for authentication to get current user info
    var name by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var profileImageUrl by remember { mutableStateOf("") } // url of the user's profile picture
    var profileImageUri by remember { mutableStateOf<android.net.Uri?>(null) } //
    // opens the device image picker so the user can change their picture
    val imagePicker =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            profileImageUri = uri
        }

    var classesList by remember { mutableStateOf(listOf<String>()) }
    var interestsList by remember { mutableStateOf(listOf<String>()) }

    var classesInput by remember { mutableStateOf("") }
    var interestsInput by remember { mutableStateOf("") }
    var loadingProfile by remember { mutableStateOf(true) } // Tracks whether the profile is being loaded or not
    var isEditing by remember { mutableStateOf(false) } // Tracks whether the user is editing their profile or not
    var saveMessage by remember { mutableStateOf("") }

    // Variables will hold the original data from the database when user cancels editing
    var initialName by remember { mutableStateOf("") }
    var initialMajor by remember { mutableStateOf("") }
    var initialBio by remember { mutableStateOf("") }
    var initialClasses by remember { mutableStateOf(listOf<String>()) }
    var initialInterests by remember { mutableStateOf(listOf<String>()) }
    var initialProfileImageUrl by remember { mutableStateOf("") }


    // Firebase instance to access database and get user data according to the uid
    val db = FirebaseFirestore.getInstance()
    val uid = currentUser?.uid

    // Load user profile data from Firestore database using the current uid
    LaunchedEffect(uid) {
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    // Fill in the variables with the data from the database
                    name = document.getString("name") ?: ""
                    major = document.getString("major") ?: ""
                    bio = document.getString("bio") ?: ""
                    profileImageUrl = document.getString("profileImageUrl") ?: "" // Load the saved Url from Firestore

                    // Load lists from database
                    classesList = (document.get("classes") as? List<*>)?.filterIsInstance<String>()
                        ?: emptyList()
                    interestsList =
                        (document.get("interests") as? List<*>)?.filterIsInstance<String>()
                            ?: emptyList()

                    // Convert the lists to strings for editing
                    classesInput = classesList.joinToString(", ")
                    interestsInput = interestsList.joinToString(", ")

                    // Holds original values for the cancel feature in editing profile
                    initialName = name
                    initialMajor = major
                    initialBio = bio
                    initialClasses = classesList
                    initialInterests = interestsList
                    initialProfileImageUrl = profileImageUrl

                    loadingProfile = false // Loading complete so now the UI can be displayed
                }
                .addOnFailureListener {
                    loadingProfile = false // Stop loading if there is an error
                }
        } else {
            loadingProfile = false
        }
    }

    Scaffold(
        topBar = { // Made the top bar visible while loading the data from database
            CenterAlignedTopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = toHomeScreen) {
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
        // Shows profile info only after Firestore data is done loading
        if (!loadingProfile) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile picture/ Profile header
                Surface(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Show profile picture if it exists, if not then default
                        when {
                            // When a new image is selected show the preview before saving
                            profileImageUri != null -> {
                                AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // If no new image is selected show the saved profile picture
                            profileImageUrl.isNotBlank() -> {
                                AsyncImage(
                                    model = profileImageUrl,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            // default icon
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile picture",
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (name.isNotBlank()) name else "Name not added yet",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Toggle between view and edit profile modes
                if (!isEditing) {
                    // Show profile in the view mode where you can't edit
                    // User info card
                    ProfileCard(
                        title = "Personal Info"
                    ) {
                        ProfileInfo(label = "Name", value = name.ifBlank { "Not added yet" })
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileInfo(label = "Major", value = major.ifBlank { "Not added yet" })
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileInfo(
                            label = "Classes",
                            value = if (classesList.isNotEmpty()) classesList.joinToString(", ") else "Not added yet"
                        ) // Convert the list to a string for display
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileInfo(
                            label = "Interests",
                            value = if (interestsList.isNotEmpty()) interestsList.joinToString(", ") else "Not added yet"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bio card
                    ProfileCard(
                        title = "About"
                    ) {
                        Text(
                            text = if (bio.isNotBlank()) bio else "No bio added yet.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))


                    // Edit profile button to update user info
                    Button(
                        onClick = {
                            saveMessage = ""
                            isEditing = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Profile")
                    }
                } else {
                    // Change profile picture button in edit mode
                    Button(onClick = {
                        imagePicker.launch("image/*")
                    }) {
                        Text("Change Profile Picture")
                    }
                    // Edit profile mode where user can edit their info
                    ProfileCard(title = "Edit Personal Info") {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = major,
                            onValueChange = { major = it },
                            label = { Text("Major") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = classesInput,
                            onValueChange = { classesInput = it },
                            label = { Text("Classes") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = interestsInput,
                            onValueChange = { interestsInput = it },
                            label = { Text("Interests") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ProfileCard(title = "Edit About") {
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                // Restore original values if user cancels editing
                                name = initialName
                                major = initialMajor
                                bio = initialBio
                                classesList = initialClasses
                                interestsList = initialInterests
                                profileImageUrl = initialProfileImageUrl
                                profileImageUri = null


                                classesInput = initialClasses.joinToString(", ")
                                interestsInput = initialInterests.joinToString(", ")
                                saveMessage = ""
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (uid != null) {
                                    // Convert user input strings to lists before saving to database
                                    classesList = classesInput
                                        .split(",","\n") // Split the input string by commas
                                        .map { classNames -> classNames.trim() } // remove spaces
                                        .filter { className -> className.isNotBlank() } // remove empty strings

                                    interestsList = interestsInput
                                        .split(",","\n")
                                        .map { interestName -> interestName.trim() }
                                        .filter { interestName -> interestName.isNotBlank() }

                                    // Firebase storage reference where the pictures are uploaded
                                    val storageReference = FirebaseStorage.getInstance().reference

                                    fun saveProfileData(newProfileImageUrl: String) {
                                        // Save updated user data to database
                                        val updateUserData = hashMapOf(
                                            "name" to name,
                                            "major" to major,
                                            "bio" to bio,
                                            "classes" to classesList,
                                            "interests" to interestsList,
                                            "profileImageUrl" to newProfileImageUrl
                                        )

                                        // Update only the fields that have been changed in the users document
                                        db.collection("users")
                                            .document(uid)
                                            .update(updateUserData as Map<String, Any>)
                                            .addOnSuccessListener {
                                                profileImageUrl = newProfileImageUrl
                                                initialName = name
                                                initialMajor = major
                                                initialBio = bio
                                                initialClasses = classesList
                                                initialInterests = interestsList
                                                initialProfileImageUrl = newProfileImageUrl

                                                profileImageUri = null
                                                saveMessage = "Profile updated!"
                                                isEditing = false
                                            }
                                            .addOnFailureListener {
                                                saveMessage = "Failed to update profile."
                                            }
                                    }
                                    // Uploads the selected image to Firebase Storage before saving the Url to the database
                                    if (profileImageUri != null) {
                                        val imageReference =
                                            storageReference.child("profileImages/${uid}.jpg")

                                        imageReference.putFile(profileImageUri!!)
                                            .addOnSuccessListener {
                                                // Gets the download URL of the uploaded image to store in Firestore
                                                imageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                                                    saveProfileData(downloadUri.toString())
                                                }.addOnFailureListener {
                                                    saveMessage = "Failed to get image URL."
                                                }
                                            }
                                            .addOnFailureListener {
                                                saveMessage = "Failed to upload image."
                                            }
                                    } else {
                                        // If no new image is selected then save the rest of the profile
                                        saveProfileData(profileImageUrl)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                }

                if (saveMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = saveMessage,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@Composable
fun ProfileCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

@Composable
fun ProfileInfo(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3
        )
    }
}