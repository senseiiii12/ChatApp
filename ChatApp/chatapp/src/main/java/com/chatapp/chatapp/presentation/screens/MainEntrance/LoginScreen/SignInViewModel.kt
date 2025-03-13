package com.chatapp.chatapp.presentation.screens.MainEntrance.LoginScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.domain.AuthRepository
import com.chatapp.chatapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _signInState = MutableStateFlow(SignInState())
    val signInState: StateFlow<SignInState> = _signInState.asStateFlow()


    fun loginUser(email: String, password: String, onSuccessLogin: () -> Unit) =
        viewModelScope.launch {
            repository.loginUser(email, password).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _signInState.update { it.copy(isSuccess = "Sign in Success", isLoading = false, isError = null) }
                        onSuccessLogin()
                    }
                    is Resource.Loading -> {
                        _signInState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Error -> {
                        _signInState.update { it.copy(isError = "Incorrect Data", isLoading = false) }
                    }
                }
            }
        }

    fun getCurrentUserUID(): String? {
        return repository.getCurrentUserUID()
    }

    fun forgotPassword(email: String) = viewModelScope.launch {
        repository.forgotPassword(email)
    }

}