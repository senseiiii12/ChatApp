package com.chatapp.chatapp.features.my_friends.presentation

import com.chatapp.chatapp.features.auth.domain.User

data class MyFriendsState(
    val data: List<User>? = emptyList(),
    val isLoading: Boolean = false,
    val isError: String = ""
)
