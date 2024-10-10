package com.chatapp.chatapp.presentation.screens.HomePage

import androidx.compose.runtime.Stable
import com.chatapp.chatapp.domain.models.User
@Stable
data class UserListState(
    val isLoading: Boolean = false,
    val isSuccess: List<User> = emptyList(),
    val isError: String? = ""
)
