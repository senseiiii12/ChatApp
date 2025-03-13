package com.chatapp.chatapp.presentation.screens.MainEntrance.LoginScreen

import androidx.compose.runtime.Immutable

@Immutable
data class SignInState(
    val isLoading: Boolean = false,
    val isSuccess: String? = "",
    val isError: String? = "",
)