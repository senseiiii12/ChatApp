package com.chatapp.chatapp.domain.models

import androidx.compose.runtime.Stable
import java.util.Date

@Stable
data class Message(
    val userId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val messageId: String = "",
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus { SENT, DELIVERED, READ }