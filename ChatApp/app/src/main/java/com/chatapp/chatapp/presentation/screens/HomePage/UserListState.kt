package com.chatapp.chatapp.presentation.screens.HomePage

import com.chatapp.chatapp.domain.models.User

data class UserListState(
    val isLoading: Boolean = false,
    val isSuccess: List<User> = emptyList(),
    val isError: String? = ""
)
