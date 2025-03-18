package com.chatapp.chatapp.features.chat_rooms.domain

import com.chatapp.chatapp.features.chat_rooms.presentation.new_state.ChatRoomsState
import kotlinx.coroutines.flow.Flow

interface ChatRoomsRepository {

    suspend fun getChatRoomsId(currentUserId: String): Flow<List<ChatRoomsState>>
    suspend fun getUserChatRooms(userId: String): Flow<List<ChatRoomsState>>

}