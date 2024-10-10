package com.chatapp.chatapp.domain


import com.chatapp.chatapp.domain.models.Message
import kotlinx.coroutines.flow.Flow


interface MessageRepository {

    suspend fun sendMessage(chatId: String, currentUserId: String, messageText: String)
    suspend fun markMessageAsRead(chatId: String, messageId: String)
    fun listenForMessages(chatId: String, onMessagesChanged: (List<Message>, List<Message>, List<String>) -> Unit)
    suspend fun listenForMessagesInChats(chatIds: List<String>): Flow<Map<String, List<Message>>>
    suspend fun deleteMessage(chatId: String, messageId: String)
    suspend fun onSaveEditMessage(chatId: String, messageId: String, newMessageText: String)
}