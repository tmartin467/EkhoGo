package com.example.ekhogo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Red80,
    secondary = RedGrey80,
    tertiary = Rose80,
    background = Color(0xFF1A1111),
    surface = Color(0xFF1A1111),
    primaryContainer = Red40,
    secondaryContainer = MessageBubbleGrey,
    tertiaryContainer = Rose40,
    onPrimary = Color(0xFF690005),
    onSecondary = Color(0xFF2C1513),
    onTertiary = Color(0xFF3B0908),
    onBackground = Color(0xFFFFEDEA),
    onSurface = Color(0xFFFFEDEA),
    onPrimaryContainer = Color.White,
    onSecondaryContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Red40,
    secondary = RedGrey80,
    tertiary = Rose40,
    background = RedSurface,
    surface = RedSurface,
    primaryContainer = Red40,
    secondaryContainer = MessageBubbleGrey,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = RedOnSurface,
    onSurface = RedOnSurface,
    onPrimaryContainer = Color.White,
    onSecondaryContainer = Color.White
)

@Composable
fun EkhoGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
