package com.chatapp.chatapp.features.search_user.presentation

import com.chatapp.chatapp.core.domain.models.FriendRequest
import com.chatapp.chatapp.features.auth.domain.User

data class UserWithFriendRequest(
    val user: User,
    val incomingRequest: FriendRequest? = null, // запрос от него тебе
    val outgoingRequest: FriendRequest? = null  // твой запрос ему
) {
    val haveIncomingRequest: Boolean
        get() = incomingRequest != null
    val canSendRequest: Boolean
        get() = incomingRequest == null && outgoingRequest == null
    val haveOutgoingRequest: Boolean
        get() = outgoingRequest != null
}