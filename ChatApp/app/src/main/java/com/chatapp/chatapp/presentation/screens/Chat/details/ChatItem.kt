package com.chatapp.chatapp.presentation.screens.Chat.details

import com.chatapp.chatapp.domain.models.Message

sealed class ChatItem {
    data class MessageItem(val message: Message) : ChatItem()
    data class DateSeparatorItem(val date: String) : ChatItem()
}