package com.chatapp.chatapp.features.chat.domain

import androidx.compose.runtime.Stable

@Stable
data class Message(
    val userId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val messageId: String = "",
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus { SENT, DELIVERED, READ }