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
    val bio: String = "",
    val profileImageUrl: String = "",
    val classesList: List<String> = emptyList(),
    val status: FriendStatus = FriendStatus.NONE
)