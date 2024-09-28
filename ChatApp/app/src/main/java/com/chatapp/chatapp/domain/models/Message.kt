package com.chatapp.chatapp.domain.models

import androidx.compose.runtime.Stable
import java.util.Date
import javax.annotation.concurrent.Immutable

@Stable
data class Message(
    val userId: String = "",
    val text: String = "",
    val timestamp: Date = Date(0),
    val messageId: String = "",
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}