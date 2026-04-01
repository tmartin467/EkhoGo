package com.example.ekhogo.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onGoToRegister: () -> Unit) {
    val auth = FirebaseAuth.getInstance() // Firebase Authentication instance


    var email by remember { mutableStateOf("") } // Variables for user input
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }


    Column(modifier = Modifier.fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Login screen title
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Email input field
        TextField(
            value = email,
            onValueChange = { email = it }, // Updates the email variable as the user types
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        // Password input field
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation() // Hides the password
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login button
        Button(
            onClick = {
                    auth.signInWithEmailAndPassword(email, password) // Firebase sign in authentication request
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                onLoginSuccess() //goes to homepage on successful login
                            } else {
                                errorMessage = "Invalid email or password"
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        Spacer(modifier = Modifier.height(8.dp))

        // Button to navigate to the register screen
        Button(
            onClick = onGoToRegister,
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Register")
        }
        // Error message if login fails
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}