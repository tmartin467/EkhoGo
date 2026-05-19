package com.example.ekhogo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Red40,
    secondary = Color(0xFF2F3336),
    tertiary = Rose40,

    background = Color(0xFF4F4F4F),
    surface = Color(0xFF383838),

    primaryContainer = Red40,
    secondaryContainer = Color(0xFF2B2B2B),
    tertiaryContainer = Color(0xFF2F3336),

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,

    onBackground = Color(0xFFE7E9EA),
    onSurface = Color(0xFFE7E9EA),

    onPrimaryContainer = Color.White,
    onSecondaryContainer = Color(0xFFE7E9EA),
    onTertiaryContainer = Color(0xFFE7E9EA)
)

private val LightColorScheme = lightColorScheme(
    primary = Red40,
    secondary = RedGrey80,
    tertiary = Rose40,
    background = RedSurface,
    surface = RedSurface,
    primaryContainer = Red40,
    secondaryContainer = Color(0xFF424242),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = RedOnSurface,
    onSurface = RedOnSurface,
    onPrimaryContainer = Color.White,
    onSecondaryContainer = Color(0xFF231919)
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
