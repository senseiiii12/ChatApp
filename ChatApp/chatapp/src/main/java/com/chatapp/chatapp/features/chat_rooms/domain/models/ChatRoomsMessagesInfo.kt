package com.chatapp.chatapp.features.chat_rooms.domain.models

import com.chatapp.chatapp.features.chat.domain.Message

data class ChatRoomsMessagesInfo(
    val lastMessage: Message? = Message(),
    val unreadMessageCount: Int = 0
)
