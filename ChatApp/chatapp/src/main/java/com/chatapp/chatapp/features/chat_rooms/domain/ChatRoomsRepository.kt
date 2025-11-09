package com.chatapp.chatapp.features.chat_rooms.domain

import com.chatapp.chatapp.features.chat_rooms.domain.models.ChatRooms
import kotlinx.coroutines.flow.Flow

interface ChatRoomsRepository {

    /**
     * Получить все чаты пользователя с полной информацией в реальном времени
     *
     * Возвращает Flow со списком чатов, который автоматически обновляется при:
     * - Получении нового сообщения
     * - Изменении статуса прочтения сообщений
     * - Изменении онлайн статуса собеседника
     * - Изменении данных собеседника
     *
     * Каждый объект ChatRooms содержит:
     * - chatId: ID чата
     * - otherUser: Данные собеседника
     * - isOnline: Статус онлайн собеседника
     * - lastMessage: Последнее сообщение в чате
     * - unreadMessageCount: Количество непрочитанных сообщений
     *
     * @param userId ID текущего пользователя
     * @return Flow<List<ChatRooms>> - поток с обновляемым списком чатов
     */
    suspend fun getUserChatRooms(userId: String): Flow<List<ChatRooms>>
}