package com.chatapp.chatapp.presentation.screens.MainEntrance.RegisterScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.domain.AuthRepository
import com.chatapp.chatapp.domain.FirebaseDatabaseRepository
import com.chatapp.chatapp.util.Resource
import com.google.firebase.firestore.FieldValue
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseDatabaseRepository: FirebaseDatabaseRepository
) : ViewModel() {

    val _signUpState = Channel<SignUpState>()
    val singUpState = _signUpState.receiveAsFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> get() = _showBottomSheet

    fun showSheet() {
        _showBottomSheet.value = true
    }

    fun hideSheet() {
        _showBottomSheet.value = false
    }

    fun registerUser(avatar: String, name: String,email: String, password: String) = viewModelScope.launch {
        authRepository.registerUser(email, password).collect { result ->
            when (result) {
                is Resource.Success -> {
                    _signUpState.send(
                        SignUpState(
                            isSuccess = "SignUp Success",
                            isLoading = false
                        )
                    )
                    val user = mapOf(
                        "userId" to authRepository.getCurrentUserUID(),
                        "avatar" to avatar,
                        "name" to name,
                        "email" to email,
                        "password" to password,
                        "lastSeen" to FieldValue.serverTimestamp()
                    )
                    firebaseDatabaseRepository.saveUserToDatabase(user)
                }
                is Resource.Loading -> {
                    _signUpState.send(SignUpState(isLoading = true))
                }
                is Resource.Error -> {
                    _signUpState.send(SignUpState(isError = result.message))
                }
            }
        }
    }
}