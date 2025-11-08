package com.chatapp.chatapp.features.chat.domain

import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow

interface MessageRepository {

    /**
     * Отправить сообщение в чат
     */
    suspend fun sendMessage(
        chatId: String,
        currentUserId: String,
        messageText: String
    ): Result<Unit>

    /**
     * Пометить сообщение как прочитанное
     */
    suspend fun markMessageAsRead(
        chatId: String,
        messageId: String
    ): Result<Unit>

    /**
     * Удалить выбранные сообщения
     */
    suspend fun deleteMessage(
        chatId: String,
        selectedMessages: List<Message>
    ): Result<Unit>

    /**
     * Сохранить отредактированное сообщение
     */
    suspend fun onSaveEditMessage(
        chatId: String,
        messageId: String,
        newMessageText: String
    ): Result<Unit>

    /**
     * Слушать изменения сообщений в чате (callback версия)
     */
    fun listenMessagesInCurrentChat(
        chatId: String,
        onMessagesChanged: (
            newMessages: List<Message>,
            updatedMessages: List<Message>,
            removedMessagesIds: List<String>
        ) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ): ListenerRegistration

    /**
     * Слушать изменения сообщений в чате (Flow версия)
     */
    fun listenMessagesFlow(chatId: String): Flow<MessageChanges>
}

/**
 * Data class для изменений сообщений
 */
data class MessageChanges(
    val newMessages: List<Message>,
    val updatedMessages: List<Message>,
    val removedMessagesIds: List<String>
)