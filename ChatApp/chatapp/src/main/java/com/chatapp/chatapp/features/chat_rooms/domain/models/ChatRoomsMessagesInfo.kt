package com.chatapp.chatapp.features.chat_rooms.domain.models

import com.chatapp.chatapp.features.chat.domain.Message

data class ChatRoomsMessagesInfo(
    val messages: List<Message> = emptyList(),
    val unreadCount: Int = 0
)
