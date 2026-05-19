package com.example.ekhogo.message

import kotlin.collections.emptyList

data class ConversationPreview(
    val conversationId: String,
    val otherUserId: String,
    val otherUserName: String,
    val lastMessage: String,
    val deletedFor: List<String>,
    val isGroup: Boolean
)