package com.chatapp.chatapp.features.friend_requests.presentation

import androidx.compose.runtime.Stable

@Stable
data class RequestsInFriendScreenState(
    val requestsInFriendItemData: List<RequestsInFriendItemState> = emptyList(),
    val isLoading: Boolean = false,
    val isError: String = ""
)
