package com.chatapp.chatapp.features.chat.presentation.details

import com.chatapp.chatapp.features.chat.domain.Message

sealed class ChatItem {
    data class MessageItem(val message: Message) : ChatItem()
    data class DateSeparatorItem(val date: String) : ChatItem()
}