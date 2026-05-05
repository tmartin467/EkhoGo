package com.example.ekhogo.message

data class ChatMessage(
    val text: String,
    val isSentByMe: Boolean,
    val id: String
)