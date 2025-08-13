package com.chatapp.chatapp.features.friend_requests.presentation

data class RequestsInFriendScreenState(
    val requestsInFriendItemData: List<RequestsInFriendItemState> = emptyList(),
    val isLoading: Boolean = false,
    val isError: String = ""
)
