package com.chatapp.chatapp.presentation.screens.RegisterScreen


data class SignUpState(
    val isLoading: Boolean = false,
    val isSuccess: String? = "",
    val isError: String? = ""

)