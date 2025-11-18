package com.chatapp.chatapp.features.auth.presentation.Validator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ValidateSignInState(
    val errorEmailLogin: String = "",
    val errorPasswordLogin: String = "",
){
    val validationSuccess = errorEmailLogin.isEmpty() && errorPasswordLogin.isEmpty()
}
data class ValidateSignUpState(
    val errorEmailRegister: String = "",
    val errorPasswordRegister: String = ""
){
    val validationSuccess = errorEmailRegister.isEmpty() && errorPasswordRegister.isEmpty()
}
data class ValidateForgotPasswordState(
    val errorForgotEmail: String = ""
){
    val validationSuccess = errorForgotEmail.isEmpty()
}

class ValidateViewModel : ViewModel() {

    private val _validationSingIn = MutableStateFlow(ValidateSignInState())
    val validationSingIn: StateFlow<ValidateSignInState> = _validationSingIn

    private val _validationSignUp = MutableStateFlow(ValidateSignUpState())
    val validationSignUp: StateFlow<ValidateSignUpState> = _validationSignUp

    private val _validationForgotPassword = MutableStateFlow(ValidateForgotPasswordState())
    val validationForgotPassword: StateFlow<ValidateForgotPasswordState> = _validationForgotPassword

    fun validateForgotEmail(email: String) {
        _validationForgotPassword.value = _validationForgotPassword.value.copy(
            errorForgotEmail = Validator.validateEmail(email)?.message.orEmpty()
        )
    }

    fun validateEmailLogin(email: String) {
        _validationSingIn.value = _validationSingIn.value.copy(
            errorEmailLogin = Validator.validateEmail(email)?.message.orEmpty()
        )
    }

    fun validatePasswordLogin(password: String) {
        _validationSingIn.value = _validationSingIn.value.copy(
            errorPasswordLogin = Validator.validatePassword(password)?.message.orEmpty()
        )
    }

    fun validateEmailRegister(email: String) {
        _validationSignUp.value = _validationSignUp.value.copy(
            errorEmailRegister = Validator.validateEmail(email)?.message.orEmpty()
        )
    }

    fun validatePasswordRegister(password: String) {
        _validationSignUp.value = _validationSignUp.value.copy(
            errorPasswordRegister = Validator.validatePassword(password)?.message.orEmpty()
        )
    }

}


