package com.example.ekhogo.schedule

data class ClassSchedule(
    val name: String,
    val date: String,
    val time: String,
    val location: String,
    val isWeekly: Boolean
)