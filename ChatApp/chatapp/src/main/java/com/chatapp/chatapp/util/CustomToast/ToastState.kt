package com.chatapp.chatapp.util.CustomToast

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ToastState {
    var isToastVisible by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    var durationMillis by mutableStateOf(2000L)
        private set

    fun showToast(message: String, durationMillis: Long = 2000L) {
        this.message = message
        this.durationMillis = durationMillis
        isToastVisible = true
    }

    fun hideToast() {
        isToastVisible = false
    }
}