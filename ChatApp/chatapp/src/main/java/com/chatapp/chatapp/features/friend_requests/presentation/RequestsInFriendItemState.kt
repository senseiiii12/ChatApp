package com.chatapp.chatapp.features.friend_requests.presentation

import com.chatapp.chatapp.core.domain.models.FriendRequest
import com.chatapp.chatapp.features.auth.domain.User

data class RequestsInFriendItemState(
    val request: FriendRequest = FriendRequest(),
    val user: User = User(),
    val isLoadingAccept: Boolean = false,
    val isSuccessAccept: Boolean = false,
    val isErrorAccept: String = "",
    val isLoadingDecline: Boolean = false,
    val isSuccessDecline: Boolean = false,
    val isErrorDecline: String = "",
)
