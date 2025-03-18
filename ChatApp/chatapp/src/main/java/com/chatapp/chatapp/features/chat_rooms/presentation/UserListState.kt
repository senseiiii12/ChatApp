package com.chatapp.chatapp.features.chat_rooms.presentation

import androidx.compose.runtime.Stable
import com.chatapp.chatapp.features.auth.domain.User
@Stable
data class UserListState(
    val isLoading: Boolean = false,
    val isSuccess: List<User> = emptyList(),
    val isError: String? = ""
)
