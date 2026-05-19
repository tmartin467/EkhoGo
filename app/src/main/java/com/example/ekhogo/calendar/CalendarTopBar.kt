package com.example.ekhogo.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.TextStyle

@Composable
fun CalendarTopBar(
    currentMonth: YearMonth,
    viewMode: CalendarViewMode,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onViewModeChange: (CalendarViewMode) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    val currentLocale = LocalLocale.current.platformLocale

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // ← Previous
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
        }

        // Month Title
        Text(
            text = "${
                currentMonth.month.getDisplayName(
                    TextStyle.FULL,
                    currentLocale
                )
            } ${currentMonth.year}",
            fontSize = 22.sp
        )

        // → Next
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
        }
    }
}

