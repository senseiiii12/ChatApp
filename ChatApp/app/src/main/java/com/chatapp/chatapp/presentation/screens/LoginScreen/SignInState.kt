package com.chatapp.chatapp.presentation.screens.LoginScreen

data class SignInState(
    val isLoading: Boolean = false,
    val isSuccess: String? = "",
    val isError: String? = ""
)