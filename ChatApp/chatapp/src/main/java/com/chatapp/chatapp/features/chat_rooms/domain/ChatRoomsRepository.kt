package com.chatapp.chatapp.features.chat_rooms.domain


import com.chatapp.chatapp.features.chat_rooms.domain.models.ChatRoomsMessagesInfo
import com.chatapp.chatapp.features.chat_rooms.domain.models.ChatRooms
import kotlinx.coroutines.flow.Flow

interface ChatRoomsRepository {

    suspend fun getAllChatRoomsId(currentUserId: String): Flow<List<String>>
    suspend fun getUserChatRooms(userId: String): Flow<List<ChatRooms>>
    suspend fun lastMessagesListner(chatIds: List<String>): Flow<Map<String, ChatRoomsMessagesInfo>>
}