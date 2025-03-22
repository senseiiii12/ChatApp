package com.chatapp.chatapp.features.chat_rooms.domain

import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat_rooms.presentation.ChatRoomsState
import kotlinx.coroutines.flow.Flow

interface ChatRoomsRepository {

    suspend fun getAllChatRoomsId(currentUserId: String): Flow<List<String>>
    suspend fun getUserChatRooms(userId: String): Flow<List<ChatRoomsState>>
    suspend fun listenForMessagesInChats(chatIds: List<String>): Flow<Map<String, List<Message>>>
}