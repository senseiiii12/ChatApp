package com.chatapp.chatapp.presentation


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ValidateViewModel: ViewModel() {

    var errorForgotEmail = mutableStateOf("")
    var errorEmail = mutableStateOf("")
    var errorPassword = mutableStateOf("")

    var errorEmailRegister = mutableStateOf("")
    var errorPasswordRegister = mutableStateOf("")


    fun validateForgotEmail(email: String) {
        if (email.isEmpty()) {
            errorForgotEmail.value = "Email cannot be empty"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorForgotEmail.value = "Invalid email format"
        } else {
            errorForgotEmail.value = ""
        }
    }
    fun validateEmail(email: String) {
        if (email.isEmpty()) {
            errorEmail.value = "Email cannot be empty"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmail.value = "Invalid email format"
        } else {
            errorEmail.value = ""
        }
    }
    fun validateEmailRegister(email: String) {
        if (email.isEmpty()) {
            errorEmailRegister.value = "Email cannot be empty"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmailRegister.value = "Invalid email format"
        } else {
            errorEmailRegister.value = ""
        }
    }

    fun validatePassword(password: String) {
        val invalidChars = """[^a-zA-Z0-9!@#\$%^&*()_+\[\]{}|\\,.<>?/~`-]""".toRegex()
        when {
            password.length < 8 -> {
                errorPassword.value = "Password must be at least 8 characters long"
            }
            password.all { it.isDigit() } -> {
                errorPassword.value = "Password cannot be only numbers"
            }
            password.all { it.isLetter() } -> {
                errorPassword.value = "Password cannot be only letters"
            }
            invalidChars.containsMatchIn(password) -> {
                errorPassword.value = "Password contains invalid characters"
            }
            else -> {
                errorPassword.value = ""
            }
        }
    }
    fun validatePasswordRegister(password: String) {
        val invalidChars = """[^a-zA-Z0-9!@#\$%^&*()_+\[\]{}|\\,.<>?/~`-]""".toRegex()
        when {
            password.length < 8 -> {
                errorPasswordRegister.value = "Password must be at least 8 characters long"
            }
            password.all { it.isDigit() } -> {
                errorPasswordRegister.value = "Password cannot be only numbers"
            }
            password.all { it.isLetter() } -> {
                errorPasswordRegister.value = "Password cannot be only letters"
            }
            invalidChars.containsMatchIn(password) -> {
                errorPasswordRegister.value = "Password contains invalid characters"
            }
            else -> {
                errorPasswordRegister.value = ""
            }
        }
    }
}