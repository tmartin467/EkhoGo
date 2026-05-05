package com.example.ekhogo.calendar

data class Event(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val timeStart: String = "",
    val timeEnd: String = "",
    val color: String = "red",
    val isAllDay: Boolean = false,
    val location: String = "",
    val notes: String = "",
    val startDate: String = "",
    val endDate: String = ""
)