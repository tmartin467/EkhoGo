package com.example.ekhogo.calendar

import androidx.compose.ui.graphics.Color

fun getEventColor(colorName: String?): Color {
    return when (colorName?.lowercase()) {
        "tomato" -> Color(0xFFD50000)
        "flamingo" -> Color(0xFFE67C73)
        "tangerine" -> Color(0xFFF4511E)
        "banana" -> Color(0xFFF6BF26)
        "sage" -> Color(0xFF33B679)
        "basil" -> Color(0xFF0B8043)
        "peacock" -> Color(0xFF039BE5)
        "blueberry" -> Color(0xFF3F51B5)
        "lavender" -> Color(0xFF7986CB)
        "grape" -> Color(0xFF8E24AA)
        "graphite" -> Color(0xFF616161)
        else -> Color(0xFF1E88E5)
    }
}