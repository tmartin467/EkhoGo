package com.example.ekhogo.message

import kotlin.collections.emptyList

data class ConversationPreview(
    val otherUserId: String,
    val otherUserName: String,
    val lastMessage: String,
    val numOfParticipants: Int,
    val deletedFor: List<String>
)