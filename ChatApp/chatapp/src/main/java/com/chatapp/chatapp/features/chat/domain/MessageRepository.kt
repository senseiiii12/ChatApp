package com.chatapp.chatapp.features.chat.domain

interface MessageRepository {

    suspend fun sendMessage(chatId: String, currentUserId: String, messageText: String)
    suspend fun markMessageAsRead(chatId: String, messageId: String)
    suspend fun deleteMessage(chatId: String, selectedMessages: List<Message>)
    suspend fun onSaveEditMessage(chatId: String, messageId: String, newMessageText: String)
    fun listenMessagesInCurrentChat(chatId: String, onMessagesChanged: (List<Message>, List<Message>, List<String>) -> Unit)
}