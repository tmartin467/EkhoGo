package com.example.ekhogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import com.example.ekhogo.ui.theme.EkhoGoTheme
import androidx.compose.runtime.*
import com.example.ekhogo.login.RegisterScreen
import com.example.ekhogo.login.LoginScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EkhoGoTheme {
                var currentScreen by remember {mutableStateOf("login")}

                when (currentScreen) {
                    "register" -> RegisterScreen(
                        onRegisterSuccess = { currentScreen = "home" },
                        onGoToLogin = { currentScreen = "login" }
                    )

                    "login" -> LoginScreen(
                        onLoginSuccess = { currentScreen = "home" },
                        onGoToRegister = { currentScreen = "register" }
                    )

                    "home" -> HomeScreen()
                }
            }
        }
    }
}