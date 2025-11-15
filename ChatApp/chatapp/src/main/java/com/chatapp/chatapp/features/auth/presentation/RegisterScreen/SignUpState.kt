package com.chatapp.chatapp.features.auth.presentation.RegisterScreen


data class SignUpState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String = ""
)