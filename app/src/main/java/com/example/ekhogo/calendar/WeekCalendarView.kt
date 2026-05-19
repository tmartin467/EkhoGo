package com.example.ekhogo.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun WeekCalendarView(
    selectedDate: LocalDate,
    events: Map<LocalDate, List<Event>>,
    onDateSelected: (LocalDate) -> Unit
) {
    val startOfWeek = selectedDate.minusDays((selectedDate.dayOfWeek.value % 7).toLong())
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDays.forEach { date ->
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()

            val eventsForDay = events.values.flatten().filter { event ->
                val start = LocalDate.parse(event.startDate.ifBlank { event.date })
                val end = LocalDate.parse(event.endDate.ifBlank { event.date })
                date >= start && date <= end
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onDateSelected(date) }
                    .padding(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.dayOfWeek.name.take(3),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(2.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isToday -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    if (eventsForDay.isNotEmpty()) {
                        val eventBars = eventsForDay.take(3)

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            eventBars.forEach { event ->
                                val start = LocalDate.parse(event.startDate.ifBlank { event.date })
                                val end = LocalDate.parse(event.endDate.ifBlank { event.date })
                                val barColor = getEventColor(event.color)

                                if (start == end) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(barColor, CircleShape)
                                    )
                                } else {
                                    val shape = when {
                                        date == start -> RoundedCornerShape(
                                            topStart = 50.dp,
                                            bottomStart = 50.dp
                                        )

                                        date == end -> RoundedCornerShape(
                                            topEnd = 50.dp,
                                            bottomEnd = 50.dp
                                        )

                                        else -> RoundedCornerShape(0.dp)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(26.dp)
                                            .height(5.dp)
                                            .background(barColor, shape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}