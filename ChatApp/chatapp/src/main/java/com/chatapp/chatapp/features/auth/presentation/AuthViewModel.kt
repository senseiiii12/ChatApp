package com.chatapp.chatapp.features.auth.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.auth.domain.AuthRepository
import com.chatapp.chatapp.features.auth.presentation.LoginScreen.SignInState
import com.chatapp.chatapp.features.auth.presentation.RegisterScreen.SignUpState
import com.chatapp.chatapp.util.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _signInState = MutableStateFlow(SignInState())
    val signInState: StateFlow<SignInState> = _signInState.asStateFlow()

    private val _signUpState = MutableStateFlow(SignUpState())
    val signUpState: StateFlow<SignUpState> = _signUpState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordState())
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    private val _showSignUpBottomSheet = MutableStateFlow(false)
    val showSignUpBottomSheet: StateFlow<Boolean> = _showSignUpBottomSheet


    fun updateSignInEmail(email: String) {
        _signInState.update { it.copy(email = email) }
    }

    fun updateSignInPassword(password: String) {
        _signInState.update { it.copy(password = password) }
    }

    fun updateSignUpName(name: String) {
        _signUpState.update { it.copy(name = name) }
    }

    fun updateSignUpEmail(email: String) {
        _signUpState.update { it.copy(email = email) }
    }

    fun updateSignUpPassword(password: String) {
        _signUpState.update { it.copy(password = password) }
    }

    fun updateForgotPasswordEmail(email: String) {
        _forgotPasswordState.update { it.copy(email = email) }
    }

    fun clearSignInError() {
        _signInState.update { it.copy(errorMessage = null) }
    }

    fun resetSignInState() {
        _signInState.value = SignInState()
    }
    fun resetSignUpState() {
        _signUpState.value = SignUpState()
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState()
    }

    fun showSignUpBottomSheet() {
        _showSignUpBottomSheet.value = true
    }

    fun hideSignUpBottomSheet() {
        _showSignUpBottomSheet.value = false
    }


    fun signInUser() {
        val email = _signInState.value.email
        val password = _signInState.value.password

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
                            it.copy(isLoading = false, isSuccess = true, errorMessage = null)
                        }
                    }
                    is Resource.Error -> {
                        _signInState.update {
                            it.copy(isLoading = false, isSuccess = false, errorMessage = result.message ?: "Неизвестная ошибка"
                            )
                        }
                    }
                }
            }
        }
    }

    fun signUpUser(context: Context, imageUri: Uri?) {
        val name = _signUpState.value.name
        val email = _signUpState.value.email
        val password = _signUpState.value.password

        viewModelScope.launch {
            _signUpState.update { it.copy(isLoading = true) }

            try {
                val avatarDeferred = async {
                    imageUri?.let {
                        val compressedImage = compressAndResizeImage(context, it)
                        val storageRef = FirebaseStorage.getInstance().reference
                            .child("images/${UUID.randomUUID()}.webp")

                        storageRef.putBytes(compressedImage).await()
                        storageRef.downloadUrl.await().toString()
                    }
                }

                val registerDeferred = async {
                    repository.registerUser(email, password).first { it !is Resource.Loading }
                }

                val avatarUrl = avatarDeferred.await()
                when (val result = registerDeferred.await()) {
                    is Resource.Success -> {
                        val user = mapOf(
                            "userId" to repository.getCurrentUserUID(),
                            "avatar" to avatarUrl,
                            "name" to name,
                            "email" to email,
                            "password" to password,
                            "online" to false,
                            "lastSeen" to FieldValue.serverTimestamp(),
                            "friends" to emptyList<String>()
                        )

                        repository.saveUserToDatabase(user)

                        _signUpState.update {
                            it.copy(isSuccess = true, isLoading = false)
                        }
                    }

                    is Resource.Error -> {
                        _signUpState.update {
                            it.copy(
                                errorMessage = result.message ?: "Unknown error",
                                isLoading = false
                            )
                        }
                    }

                    else -> Unit
                }
            } catch (e: Exception) {
                _signUpState.update {
                    it.copy(
                        errorMessage = e.message ?: "Ошибка регистрации",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun forgotPassword() {
        val email = _forgotPasswordState.value.email

        viewModelScope.launch {
            _forgotPasswordState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            try {
                repository.forgotPassword(email)
                _forgotPasswordState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _forgotPasswordState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = e.message ?: "Ошибка отправки письма"
                    )
                }
            }
        }
    }


    private fun compressAndResizeImage(
        context: Context,
        imageUri: Uri,
        quality: Int = 70,
        maxWidth: Int = 800,
        maxHeight: Int = 800
    ): ByteArray {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, maxWidth, maxHeight, true)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
        return outputStream.toByteArray()
    }
}