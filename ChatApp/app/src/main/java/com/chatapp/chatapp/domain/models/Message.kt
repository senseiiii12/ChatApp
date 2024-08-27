package com.chatapp.chatapp.domain.models

import java.util.Date

data class Message(
    val userId: String,
    val text: String,
    val timestamp: Date,
    val messageId: String,
    var isEditing: Boolean = false,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}