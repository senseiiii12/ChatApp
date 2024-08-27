package com.chatapp.chatapp.presentation.screens.RegisterScreen.ImageAvatar

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.security.auth.callback.Callback

class ImageAvatarViewModel: ViewModel() {
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri

    private val _base64String = MutableStateFlow<String?>(null)
    val base64String: StateFlow<String?> = _base64String

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }
    fun clearImageUri() {
        _imageUri.value = null
        _base64String.value = null
    }

    fun encodeImageToBase64(context: Context, uri: Uri, onComplete: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            }

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

            _base64String.value = base64String

            withContext(Dispatchers.Main) {
                onComplete(base64String)
            }
        }
    }

    fun decodeBase64ToBitmap(base64String: String?): Bitmap? {
        return base64String?.let {
            val decodedString = Base64.decode(it, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
    }
}