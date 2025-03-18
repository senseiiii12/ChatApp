package com.chatapp.chatapp.features.auth.presentation.RegisterScreen.ImageAvatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.security.auth.callback.Callback

class ImageAvatarViewModel: ViewModel() {
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }
    fun clearImageUri() {
        _imageUri.value = null
    }

    fun uploadImageToFirebase(imageUri: Uri, callback: (String) -> Unit) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        val fileRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        fileRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    Log.d("Firebase", "URL: $downloadUrl")
                    callback(downloadUrl)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Ошибка загрузки", e)
            }
    }
}