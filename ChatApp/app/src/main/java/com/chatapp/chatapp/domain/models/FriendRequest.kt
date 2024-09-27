package com.chatapp.chatapp.domain.models

data class FriendRequest(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val status: String = "pending",
)
