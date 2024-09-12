package com.chatapp.chatapp.presentation.screens.MainEntrance.LoginScreen

data class SignInState(
    val isLoading: Boolean = false,
    val isSuccess: String? = "",
    val isError: String? = ""
)