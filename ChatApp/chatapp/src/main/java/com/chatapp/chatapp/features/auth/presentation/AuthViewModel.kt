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

    // Состояние для логина
    private val _signInState = MutableStateFlow(SignInState())
    val signInState: StateFlow<SignInState> = _signInState.asStateFlow()

    // Состояние для регистрации
    private val _signUpState = MutableStateFlow(SignUpState())
    val signUpState: StateFlow<SignUpState> = _signUpState.asStateFlow()

    // Состояние для восстановления пароля
    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordState())
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState.asStateFlow()

    // Для управления bottom sheet регистрации
    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet


    fun updateSignInEmail(email: String){
        _signInState.update { it.copy(email = email) }
    }
    fun updateSignInPassword(password: String){
        _signInState.update { it.copy(password = password) }
    }

    fun updateSignUpName(name: String){
        _signUpState.update { it.copy(name = name) }
    }
    fun updateSignUpEmail(email: String){
        _signUpState.update { it.copy(email = email) }
    }
    fun updateSignUpPassword(password: String){
        _signUpState.update { it.copy(password = password) }
    }


    // === МЕТОДЫ ДЛЯ ЛОГИНА ===

    fun signInUser(email: String, password: String) {
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

    // === МЕТОДЫ ДЛЯ РЕГИСТРАЦИИ ===

    fun signUpUser(
        context: Context,
        imageUri: Uri?,
        name: String,
        email: String,
        password: String
    ) {
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

    fun resetSignUpState() {
        _signUpState.value = SignUpState()
    }

    // === МЕТОДЫ ДЛЯ ВОССТАНОВЛЕНИЯ ПАРОЛЯ ===

    fun forgotPassword(email: String) {
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

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState()
    }

    // === УПРАВЛЕНИЕ BOTTOM SHEET ===

    fun showSheet() {
        _showBottomSheet.value = true
    }

    fun hideSheet() {
        _showBottomSheet.value = false
    }

    // === ОБЩИЕ МЕТОДЫ ===

    fun getCurrentUserUID(): String? {
        return repository.getCurrentUserUID()
    }

    // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

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