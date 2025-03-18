package com.chatapp.chatapp.features.search_user.domain

import com.chatapp.chatapp.features.auth.domain.User


data class FriendRequestWithUser(
    val friendRequest: FriendRequest,
    val user: User
)
