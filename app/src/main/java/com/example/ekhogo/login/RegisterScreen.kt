package com.example.ekhogo.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ekhogo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onGoToLogin: () -> Unit) {
    val auth = FirebaseAuth.getInstance() // Firebase Authentication Instance

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.homepage_icon),
            contentDescription = "EkhoGo dolphin logo",
            modifier = Modifier.size(160.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                errorMessage = ""
                // Checking for empty fields first
                if (email.trim().isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }

                // Check if password matches
                if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                    return@Button
                }

                // Only reaches Firebase if everything is valid
                auth.createUserWithEmailAndPassword(
                    email.trim(),
                    password
                ) // Create user with Firebase
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser // Get current user

                            if (user == null) {
                                errorMessage = "Registration failed: user not found"
                                return@addOnCompleteListener
                            }

                            val db =
                                FirebaseFirestore.getInstance()  // Firebase Firestore Instance

                            // Create user data in Firestore leaving some fields blank to be filled in later by the user
                            val userData = hashMapOf(
                                "uid" to user.uid,
                                "email" to user.email,
                                "name" to "",
                                "major" to "",
                                "year" to "",
                                "bio" to "",
                                "friends" to listOf<String>(),
                                "profilePictureUrl" to ""
                            )

                            // Add user data to Firestore in users collection/uid document
                            db.collection("users")
                                .document(user.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    // Only move forward if Firestore write succeeds
                                    onRegisterSuccess()
                                }
                                .addOnFailureListener { e ->
                                    // Show error if something goes wrong saving the profile
                                    errorMessage =
                                        e.localizedMessage ?: "Failed to save profile"
                                }

                        } else {
                            // Handle registration failure
                            errorMessage =
                                task.exception?.localizedMessage ?: "Registration failed"
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Button to navigate to the login screen
        Button(
            onClick = onGoToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        // Display Error message if registration fails
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}