package com.chatapp.chatapp.features.chat.presentation.details.inputField

import com.chatapp.chatapp.features.chat.domain.Message

data class ChatInputFieldState(
    val defaultInputMessage: String = "",
    val editingInputMessage: String = "",
    val isEditingMessage: Boolean = false,
    val editingMessage: Message? = null
)
