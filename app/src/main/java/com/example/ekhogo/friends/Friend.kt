package com.example.ekhogo.friends

enum class FriendStatus {
    NONE,
    REQUEST_SENT,
    REQUEST_RECEIVED,
    FRIENDS
}

data class Friend(
    val id: String,
    val name: String,
    val major: String,
    val status: FriendStatus = FriendStatus.NONE
)