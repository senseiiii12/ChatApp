package com.chatapp.chatapp.features.auth.presentation.RegisterScreen.ImageAvatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.ByteArrayOutputStream
import java.util.UUID

class ImageAvatarViewModel : ViewModel() {
    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri: StateFlow<Uri?> = _imageUri

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun clearImageUri() {
        _imageUri.value = null
    }

    fun uploadImageToFirebase(context: Context, imageUri: Uri, callback: (String) -> Unit) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val fileRef = storageRef.child("images/${UUID.randomUUID()}.webp")

        val compressedImage = compressAndResizeImage(context, imageUri)

        fileRef.putBytes(compressedImage)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Ошибка загрузки", e)
            }
    }


    private fun compressAndResizeImage(context: Context, imageUri: Uri, quality: Int = 70, maxWidth: Int = 800, maxHeight: Int = 800): ByteArray {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, maxWidth, maxHeight, true)
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
        return outputStream.toByteArray()
    }
}