package com.example.ekhogo.calendar

import androidx.compose.ui.graphics.Color

fun getEventColor(colorName: String?): Color {
    return when (colorName?.lowercase()) {
        "red" -> Color(0xFFE53935)
        "blue" -> Color(0xFF1E88E5)
        "green" -> Color(0xFF43A047)
        "yellow" -> Color(0xFFFDD835)
        "orange" -> Color(0xFFFB8C00)
        "purple" -> Color(0xFF8E24AA)
        "cyan" -> Color(0xFF00ACC1)
        "gray" -> Color(0xFF757575)
        else -> Color(0xFF1E88E5)
    }
}