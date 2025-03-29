package com.chatapp.chatapp.features.auth.presentation.RegisterScreen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.auth.domain.AuthRepository
import com.chatapp.chatapp.core.domain.UsersRepository
import com.chatapp.chatapp.util.Resource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val _signUpState = Channel<SignUpState>()
    val singUpState = _signUpState.receiveAsFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet

    fun showSheet() {
        _showBottomSheet.value = true
    }

    fun hideSheet() {
        _showBottomSheet.value = false
    }

    fun registerUser(avatar: String?, name: String, email: String, password: String) =
        viewModelScope.launch {
            authRepository.registerUser(email, password).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _signUpState.send(
                            SignUpState(
                                isSuccess = "Success Registration",
                                isLoading = false
                            )
                        )
                        val user = mapOf(
                            "userId" to authRepository.getCurrentUserUID(),
                            "avatar" to avatar,
                            "name" to name,
                            "email" to email,
                            "password" to password,
                            "online" to false,
                            "lastSeen" to FieldValue.serverTimestamp(),
                            "friends" to emptyList<String>(),
                        )
                        authRepository.saveUserToDatabase(user)
                    }

                    is Resource.Loading -> {

                    }

                    is Resource.Error -> {
                        _signUpState.send(
                            SignUpState(
                                isError = result.message.toString(),
                                isLoading = false
                            )
                        )
                    }
                }
            }
        }

    fun signUp(
        context: Context,
        imageUri: Uri?,
        name: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _signUpState.send(SignUpState(isLoading = true))

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val fileRef = storageRef.child("images/${UUID.randomUUID()}.webp")

            imageUri?.let {
                val compressedImage = compressAndResizeImage(context, imageUri)
                fileRef.putBytes(compressedImage)
                    .addOnSuccessListener {
                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            registerUser(uri.toString(), name, email, password)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Ошибка загрузки", e)
                    }
            } ?: registerUser(null, name, email, password)
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