package com.chatapp.chatapp.features.chat_rooms.presentation.new_state

import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.domain.Message

data class ChatRoomsState(
    val chatId: String = "",
    val otherUser: User = User(),
    val isOnline: Boolean = false,
    val lastMessage: Message = Message(),
    val unreadMessageCount: Int = 0,
)
