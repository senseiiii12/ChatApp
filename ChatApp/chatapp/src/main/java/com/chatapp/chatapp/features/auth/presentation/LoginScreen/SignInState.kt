package com.chatapp.chatapp.features.auth.presentation.LoginScreen

import androidx.compose.runtime.Immutable

@Immutable
data class SignInState(
    val isLoading: Boolean = false,
    val isSuccess: String? = "",
    val isError: String? = "",
)