package com.chatapp.chatapp.core.domain.models

import com.chatapp.chatapp.features.auth.domain.User

data class FriendRequestWithUser(
    val friendRequest: FriendRequest,
    val user: User
)