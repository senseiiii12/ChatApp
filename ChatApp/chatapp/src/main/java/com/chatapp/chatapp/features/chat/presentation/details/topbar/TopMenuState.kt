package com.chatapp.chatapp.features.chat.presentation.details.topbar

import com.chatapp.chatapp.features.chat.domain.Message

data class TopMenuState(
    val listSelectedMessages: List<Message> = emptyList(),
    val countSelectedMessage: Int = 0,
    val isOpenTopMenu: Boolean = false,
)
