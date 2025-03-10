package com.chatapp.chatapp.presentation.screens.Chat.details.inputField

import com.chatapp.chatapp.domain.models.Message

data class ChatInputFieldState(
    val defaultInputMessage: String = "",
    val editingInputMessage: String = "",
    val isEditingMessage: Boolean = false,
    val editingMessage: Message? = null
)
