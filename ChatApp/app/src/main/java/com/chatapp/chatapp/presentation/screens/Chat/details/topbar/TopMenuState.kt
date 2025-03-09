package com.chatapp.chatapp.presentation.screens.Chat.details.topbar

import com.chatapp.chatapp.domain.models.Message

data class TopMenuState(
    val listSelectedMessages: List<Message> = emptyList(),
    val countSelectedMessage: Int = 0,
    val isOpenTopMenu: Boolean = false,
)
