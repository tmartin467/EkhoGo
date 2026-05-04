package com.example.ekhogo.message

data class ConversationPreview(
    val otherUserId: String,
    val otherUserName: String,
    val lastMessage: String,
    val numOfParticipants: Int
)