package com.chatapp.chatapp.features.auth.presentation.LoginScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.auth.domain.AuthRepository
import com.chatapp.chatapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _signInState = MutableStateFlow(SignInState())
    val signInState: StateFlow<SignInState> = _signInState.asStateFlow()


    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            repository.loginUser(email, password).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _signInState.update {
                            it.copy(isLoading = true, errorMessage = null)
                        }
                    }

                    is Resource.Success -> {
                        _signInState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                errorMessage = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _signInState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = false,
                                errorMessage = result.message ?: "Неизвестная ошибка"
                            )
                        }
                    }
                }
            }
        }
    }
    fun clearSignInError() {
        _signInState.update { it.copy(errorMessage = null) }
    }

    fun resetSignInState() {
        _signInState.value = SignInState()
    }

    fun getCurrentUserUID(): String? {
        return repository.getCurrentUserUID()
    }

    fun forgotPassword(email: String) = viewModelScope.launch {
        repository.forgotPassword(email)
    }

}