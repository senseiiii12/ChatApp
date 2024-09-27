package com.chatapp.chatapp.domain.models

import java.util.Date
import javax.annotation.concurrent.Immutable

@Immutable
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