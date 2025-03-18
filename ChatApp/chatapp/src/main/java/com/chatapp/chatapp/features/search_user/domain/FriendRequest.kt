package com.chatapp.chatapp.features.search_user.domain

data class FriendRequest(
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val status: String = "pending",
)
