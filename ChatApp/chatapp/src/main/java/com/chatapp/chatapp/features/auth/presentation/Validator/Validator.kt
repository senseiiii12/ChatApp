package com.chatapp.chatapp.features.auth.presentation.Validator

import android.util.Patterns

object Validator {

    fun validateEmail(email: String): EmailValidationError? {
        return when {
            email.isEmpty() -> EmailValidationError.EMPTY_EMAIL
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> EmailValidationError.INVALID_EMAIL
            else -> null
        }
    }

    fun validatePassword(password: String): PasswordValidationError? {
        val invalidChars = """[^a-zA-Z0-9!@#\$%^&*()_+\[\]{}|\\,.<>?/~`-]""".toRegex()
        return when {
            password.isEmpty() -> PasswordValidationError.EMPTY_PASSWORD
            password.length < 8 -> PasswordValidationError.SHORT_PASSWORD
            password.all { it.isDigit() } -> PasswordValidationError.ONLY_NUMBERS
            password.all { it.isLetter() } -> PasswordValidationError.ONLY_LETTERS
            invalidChars.containsMatchIn(password) -> PasswordValidationError.INVALID_CHARACTERS
            else -> null
        }
    }
}

enum class EmailValidationError(val message: String) {
    EMPTY_EMAIL(""),
    INVALID_EMAIL("Invalid email format")
}

enum class PasswordValidationError(val message: String) {
    EMPTY_PASSWORD(""),
    SHORT_PASSWORD("Password must be at least 8 characters long"),
    ONLY_NUMBERS("Password cannot be only numbers"),
    ONLY_LETTERS("Password cannot be only letters"),
    INVALID_CHARACTERS("Password contains invalid characters")
}
