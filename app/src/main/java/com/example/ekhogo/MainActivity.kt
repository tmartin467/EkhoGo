package com.example.ekhogo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ekhogo.login.LoginScreen
import com.example.ekhogo.login.RegisterScreen
import com.example.ekhogo.profile.ProfileScreen
import com.example.ekhogo.ui.theme.EkhoGoTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)

            var themeMode by remember {
                mutableStateOf(prefs.getString("theme_mode", "system") ?: "system")
            }

            val isDarkMode = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            LaunchedEffect(themeMode) {
                prefs.edit()
                    .putString("theme_mode", themeMode)
                    .apply()
            }

            EkhoGoTheme(darkTheme = isDarkMode) {
                var currentScreen by remember { mutableStateOf("login") }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        "register" -> RegisterScreen(
                            onRegisterSuccess = { currentScreen = "home" },
                            onGoToLogin = { currentScreen = "login" }
                        )

                        "login" -> LoginScreen(
                            onLoginSuccess = { currentScreen = "home" },
                            onGoToRegister = { currentScreen = "register" }
                        )

                        "home" -> HomeScreen(
                            modifier = Modifier.padding(innerPadding),
                            onAccountLogout = {
                                FirebaseAuth.getInstance().signOut()
                                currentScreen = "login"
                            },
                            toProfileScreen = { currentScreen = "profile" },
                            isDarkMode = isDarkMode,
                            themeMode = themeMode,
                            onThemeModeChange = { themeMode = it }
                        )

                        "profile" -> ProfileScreen(
                            toHomeScreen = { currentScreen = "home" }
                        )
                    }
                }
            }
        }
    }
}