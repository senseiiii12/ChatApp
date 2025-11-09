package com.chatapp.chatapp.features.chat_rooms.data

import android.util.Log
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat.domain.MessageStatus
import com.chatapp.chatapp.features.chat_rooms.domain.ChatRoomsRepository
import com.chatapp.chatapp.features.chat_rooms.domain.models.ChatRooms
import com.chatapp.chatapp.util.extension.toMessage
import com.chatapp.chatapp.util.extension.toUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRoomsRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : ChatRoomsRepository {

    private val chatCollection = firestore.collection(COLLECTION_CHATS)
    private val usersCollection = firestore.collection(COLLECTION_USERS)

    companion object {
        private const val TAG = "ChatRoomsRepository"
        private const val COLLECTION_CHATS = "chats"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_MESSAGES = "messages"
        private const val FIELD_PARTICIPANTS = "participants"
        private const val FIELD_TIMESTAMP = "timestamp"
        private const val FIELD_STATUS = "status"
        private const val FIELD_USER_ID = "userId"
        private const val LAST_MESSAGE_LIMIT = 1L
    }

    /**
     * Получить все чаты пользователя с полной информацией в реальном времени
     * Включает: данные собеседника, последнее сообщение, количество непрочитанных
     */
    override suspend fun getUserChatRooms(userId: String): Flow<List<ChatRooms>> = callbackFlow {
        if (userId.isBlank()) {
            Log.e(TAG, "User ID is empty")
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listeners = mutableListOf<ListenerRegistration>()
        val chatRoomsMap = mutableMapOf<String, ChatRooms>()

        try {
            // 1. Получаем все чаты пользователя
            val chatIds = getChatIds(userId)

            if (chatIds.isEmpty()) {
                trySend(emptyList())
                close()
                return@callbackFlow
            }

            // 2. Для каждого чата создаем слушатели
            chatIds.forEach { chatId ->
                // Создаем начальную структуру чата
                val initialChatRoom = buildInitialChatRoom(chatId, userId)

                if (initialChatRoom != null) {
                    chatRoomsMap[chatId] = initialChatRoom

                    // Слушатель последнего сообщения
                    val lastMessageListener = createLastMessageListener(
                        chatId = chatId,
                        onUpdate = { lastMessage ->
                            chatRoomsMap[chatId] = chatRoomsMap[chatId]?.copy(
                                lastMessage = lastMessage
                            ) ?: return@createLastMessageListener
                            trySend(chatRoomsMap.values.toList())
                        }
                    )
                    listeners.add(lastMessageListener)

                    // Слушатель непрочитанных сообщений
                    val unreadListener = createUnreadMessagesListener(
                        chatId = chatId,
                        currentUserId = userId,
                        onUpdate = { unreadCount ->
                            chatRoomsMap[chatId] = chatRoomsMap[chatId]?.copy(
                                unreadMessageCount = unreadCount
                            ) ?: return@createUnreadMessagesListener
                            trySend(chatRoomsMap.values.toList())
                        }
                    )
                    listeners.add(unreadListener)

                    // Слушатель статуса онлайн собеседника
                    val otherUserId = initialChatRoom.otherUser.userId
                    val onlineListener = createUserOnlineListener(
                        chatId = chatId,
                        otherUserId = otherUserId,
                        onUpdate = { isOnline, updatedUser ->
                            chatRoomsMap[chatId] = chatRoomsMap[chatId]?.copy(
                                isOnline = isOnline,
                                otherUser = updatedUser
                            ) ?: return@createUserOnlineListener
                            trySend(chatRoomsMap.values.toList())
                        }
                    )
                    listeners.add(onlineListener)
                }
            }

            // Отправляем начальное состояние
            trySend(chatRoomsMap.values.toList())

        } catch (e: Exception) {
            Log.e(TAG, "Error setting up chat rooms listeners", e)
            close(e)
        }

        awaitClose {
            Log.d(TAG, "Cleaning up ${listeners.size} listeners")
            listeners.forEach { it.remove() }
            listeners.clear()
            chatRoomsMap.clear()
        }
    }.catch { e ->
        Log.e(TAG, "Flow error in getUserChatRooms", e)
        emit(emptyList())
    }.flowOn(Dispatchers.IO)

    /**
     * Получить ID всех чатов пользователя
     */
    private suspend fun getChatIds(userId: String): List<String> {
        return try {
            val chatsSnapshot = chatCollection
                .whereArrayContains(FIELD_PARTICIPANTS, userId)
                .get()
                .await()

            chatsSnapshot.documents.mapNotNull { it.id }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chat IDs for user: $userId", e)
            emptyList()
        }
    }

    /**
     * Построить начальную структуру чата с базовой информацией
     */
    private suspend fun buildInitialChatRoom(
        chatId: String,
        currentUserId: String
    ): ChatRooms? {
        return try {
            // 1. Получаем документ чата
            val chatDoc = chatCollection.document(chatId).get().await()

            if (!chatDoc.exists()) {
                Log.w(TAG, "Chat document doesn't exist: $chatId")
                return null
            }

            // 2. Получаем участников
            val participants = chatDoc.get(FIELD_PARTICIPANTS) as? List<String>
                ?: run {
                    Log.w(TAG, "No participants found for chat: $chatId")
                    return null
                }

            // 3. Находим собеседника
            val otherUserId = participants.firstOrNull { it != currentUserId }
                ?: run {
                    Log.w(TAG, "No other user found in chat: $chatId")
                    return null
                }

            // 4. Получаем данные собеседника
            val otherUser = fetchUserData(otherUserId)
                ?: run {
                    Log.w(TAG, "User data not found for: $otherUserId")
                    return null
                }

            // 5. Получаем последнее сообщение (синхронно, один раз)
            val lastMessage = fetchLastMessage(chatId)

            // 6. Получаем количество непрочитанных (синхронно, один раз)
            val unreadCount = fetchUnreadCount(chatId, currentUserId)

            // 7. Создаем объект чата со всеми данными
            ChatRooms(
                chatId = chatId,
                otherUser = otherUser,
                isOnline = otherUser.online,
                lastMessage = lastMessage,
                unreadMessageCount = unreadCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in buildInitialChatRoom for $chatId", e)
            null
        }
    }

    /**
     * Получить последнее сообщение в чате (одноразовый запрос)
     */
    private suspend fun fetchLastMessage(chatId: String): Message {
        return try {
            val snapshot = chatCollection.document(chatId)
                .collection(COLLECTION_MESSAGES)
                .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                .limit(LAST_MESSAGE_LIMIT)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toMessage() ?: Message()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching last message for chat: $chatId", e)
            Message()
        }
    }

    /**
     * Получить количество непрочитанных сообщений (одноразовый запрос)
     */
    private suspend fun fetchUnreadCount(chatId: String, currentUserId: String): Int {
        return try {
            val snapshot = chatCollection.document(chatId)
                .collection(COLLECTION_MESSAGES)
                .whereEqualTo(FIELD_STATUS, MessageStatus.DELIVERED.name)
                .get()
                .await()

            // Считаем только сообщения от других пользователей
            snapshot.documents.count { document ->
                val messageUserId = document.getString(FIELD_USER_ID)
                messageUserId != null && messageUserId != currentUserId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching unread count for chat: $chatId", e)
            0
        }
    }

    /**
     * Создать слушатель последнего сообщения
     */
    private fun createLastMessageListener(
        chatId: String,
        onUpdate: (Message) -> Unit
    ): ListenerRegistration {
        return chatCollection.document(chatId)
            .collection(COLLECTION_MESSAGES)
            .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
            .limit(LAST_MESSAGE_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to last message for chat: $chatId", error)
                    return@addSnapshotListener
                }

                val lastMessage = snapshot?.documents?.firstOrNull()?.toMessage() ?: Message()
                onUpdate(lastMessage)
            }
    }

    /**
     * Создать слушатель непрочитанных сообщений
     */
    private fun createUnreadMessagesListener(
        chatId: String,
        currentUserId: String,
        onUpdate: (Int) -> Unit
    ): ListenerRegistration {
        return chatCollection.document(chatId)
            .collection(COLLECTION_MESSAGES)
            .whereEqualTo(FIELD_STATUS, MessageStatus.DELIVERED.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to unread messages for chat: $chatId", error)
                    return@addSnapshotListener
                }

                val unreadCount = snapshot?.documents?.count { document ->
                    val messageUserId = document.getString(FIELD_USER_ID)
                    messageUserId != null && messageUserId != currentUserId
                } ?: 0
                Log.d(TAG, "Unread count for chat $chatId by user $currentUserId: $unreadCount")
                onUpdate(unreadCount)
            }
    }

    /**
     * Создать слушатель статуса онлайн пользователя
     */
    private fun createUserOnlineListener(
        chatId: String,
        otherUserId: String,
        onUpdate: (Boolean, User) -> Unit
    ): ListenerRegistration {
        return usersCollection.document(otherUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to user status: $otherUserId", error)
                    return@addSnapshotListener
                }

                val user = snapshot?.toUser()
                if (user != null) {
                    onUpdate(user.online, user)
                }
            }
    }

    /**
     * Получить данные пользователя по ID
     */
    private suspend fun fetchUserData(userId: String): User? {
        return try {
            val userDoc = usersCollection.document(userId).get().await()

            if (!userDoc.exists()) {
                Log.w(TAG, "User document doesn't exist: $userId")
                return null
            }

            userDoc.toUser()
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user data: $userId", e)
            null
        }
    }
}