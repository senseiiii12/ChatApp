package com.chatapp.chatapp.features.auth.presentation.Validator

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ValidateStateLogin(
    val errorEmailLogin: String = "",
    val errorPasswordLogin: String = "",
)
data class ValidateStateRegister(
    val errorEmailRegister: String = "",
    val errorPasswordRegister: String = ""
)
data class ValidateStateForgotPassword(
    val errorForgotEmail: String = ""
)

class ValidateViewModel : ViewModel() {

    private val _validationLoginState = MutableStateFlow(ValidateStateLogin())
    val validationLoginState: StateFlow<ValidateStateLogin> = _validationLoginState

    private val _validationRegisterState = MutableStateFlow(ValidateStateRegister())
    val validationRegisterState: StateFlow<ValidateStateRegister> = _validationRegisterState

    private val _validationForgotPasswordState = MutableStateFlow(ValidateStateForgotPassword())
    val validationForgotPasswordState: StateFlow<ValidateStateForgotPassword> = _validationForgotPasswordState

    fun validateForgotEmail(email: String) {
        _validationForgotPasswordState.value = _validationForgotPasswordState.value.copy(
            errorForgotEmail = Validator.validateEmail(email)?.message.orEmpty()
        )
    }

    fun validateEmailLogin(email: String) {
        _validationLoginState.value = _validationLoginState.value.copy(
            errorEmailLogin = Validator.validateEmail(email)?.message.orEmpty()
        )
    }

    fun validatePasswordLogin(password: String) {
        _validationLoginState.value = _validationLoginState.value.copy(
            errorPasswordLogin = Validator.validatePassword(password)?.message.orEmpty()
        )
    }

    fun validateEmailRegister(email: String) {
        _validationRegisterState.value = _validationRegisterState.value.copy(
            errorEmailRegister = Validator.validateEmail(email)?.message.orEmpty()
        )
    }

    fun validatePasswordRegister(password: String) {
        _validationRegisterState.value = _validationRegisterState.value.copy(
            errorPasswordRegister = Validator.validatePassword(password)?.message.orEmpty()
        )
    }

}


