package com.chatapp.chatapp.features.chat_rooms.data

import android.util.Log
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat.domain.MessageStatus
import com.chatapp.chatapp.features.chat_rooms.domain.ChatRoomsRepository
import com.chatapp.chatapp.features.chat_rooms.presentation.new_state.ChatRoomsState
import com.chatapp.chatapp.util.Resource
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class ChatRoomsRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
) : ChatRoomsRepository {

    override suspend fun getChatRoomsId(currentUserId: String): Flow<List<ChatRoomsState>> {
        return flow {
            try {
                val result = firebaseFirestore.collection("chats").get().await()
                val chatRoomsList = result.mapNotNull { document ->
                    val chatRoomId = document.getString("chatId")
                    if (chatRoomId?.contains(currentUserId) == true) {
                        ChatRoomsState(chatId = chatRoomId)
                    } else null
                }
                emit(chatRoomsList)
            } catch (e: Exception) {
                emit(emptyList())
                Log.d("getChatRoomsId", e.message.toString())
            }
        }
    }

    override suspend fun getUserChatRooms(userId: String): Flow<List<ChatRoomsState>> {
        Log.d("ChatDebug", "getUserChatRooms вызван для userId: $userId")
        return flow {
            try {
                Log.d("ChatDebug", "Начало получения чатов")
                val chatRooms = fetchUserChats(userId)
                Log.d("ChatDebug", "Чаты получены: ${chatRooms.size}")
                emit(chatRooms)
            } catch (e: Exception) {
                Log.e("ChatDebug", "Ошибка в getUserChatRooms: ${e.message}", e)
                emit(emptyList())
            }
        }.flowOn(Dispatchers.IO)
    }

    // 1. Получение всех чатов пользователя
    private suspend fun fetchUserChats(userId: String): List<ChatRoomsState> {
        Log.d("ChatDebug", "fetchUserChats для $userId")
        val chatsSnapshot = firebaseFirestore.collection("chats")
            .whereArrayContains("participants", userId)
            .get()
            .await()

        if (chatsSnapshot.isEmpty) {
            Log.w("ChatDebug", "Чатов для $userId не найдено")
            return emptyList()
        }

        return chatsSnapshot.documents.mapNotNull { chatDoc ->
            Log.d("ChatDebug", "Обработка чата ${chatDoc.id}")
            buildChatRoomState(chatDoc, userId)
        }
    }

    // 2. Построение объекта ChatRoomsState для одного чата
    private suspend fun buildChatRoomState(chatDoc: DocumentSnapshot, currentUserId: String): ChatRoomsState? {
        return try {
            val participants = chatDoc.get("participants") as? List<String> ?: emptyList()
            Log.d("ChatDebug", "Участники чата ${chatDoc.id}: $participants")

            val otherUserId = participants.firstOrNull { it != currentUserId } ?: run {
                Log.w("ChatDebug", "Другой пользователь не найден в ${chatDoc.id}")
                return null
            }

            val otherUser = fetchUserData(otherUserId) ?: return null
            val lastMessage = fetchLastMessage(chatDoc.reference)
            val unreadCount = fetchUnreadCount(chatDoc.reference, otherUserId)

            ChatRoomsState(
                chatId = chatDoc.id,
                otherUser = otherUser,
                isOnline = otherUser.online,
                lastMessage = lastMessage,
                unreadMessageCount = unreadCount
            )
        } catch (e: Exception) {
            Log.e("ChatDebug", "Ошибка в buildChatRoomState для ${chatDoc.id}: ${e.message}", e)
            null
        }
    }

    // 3. Получение данных пользователя
    private suspend fun fetchUserData(userId: String): User? {
        Log.d("ChatDebug", "fetchUserData для $userId")
        return try {
            val userDoc = firebaseFirestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (!userDoc.exists()) {
                Log.w("ChatDebug", "Пользователь $userId не существует")
                return null
            }

            Log.d("ChatDebug", "Данные пользователя $userId: ${userDoc.data}")
            User(
                userId = userDoc.id,
                avatar = userDoc.getString("avatar") ?: "",
                name = userDoc.getString("name") ?: "",
                email = userDoc.getString("email") ?: "",
                password = userDoc.getString("password") ?: "",
                online = userDoc.getBoolean("online") ?: false,
                lastSeen = userDoc.getTimestamp("lastSeen")?.toDate() ?: Date(),
                friends = userDoc.get("friends") as? List<String> ?: emptyList()
            )
        } catch (e: Exception) {
            Log.e("ChatDebug", "Ошибка в fetchUserData для $userId: ${e.message}", e)
            null
        }
    }

    // 4. Получение последнего сообщения
    private suspend fun fetchLastMessage(chatRef: DocumentReference): Message {
        Log.d("ChatDebug", "fetchLastMessage для чата ${chatRef.id}")
        return try {
            val messagesSnapshot = chatRef.collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (messagesSnapshot.isEmpty) {
                Log.d("ChatDebug", "Сообщений в чате ${chatRef.id} нет")
                return Message()
            }

            val msgDoc = messagesSnapshot.documents[0]
            Log.d("ChatDebug", "Последнее сообщение: ${msgDoc.data}")
            Message(
                userId = msgDoc.getString("userId") ?: "",
                text = msgDoc.getString("text") ?: "",
                timestamp = msgDoc.getLong("timestamp") ?: 0L,
                messageId = msgDoc.id,
                status = when (msgDoc.getString("status")?.uppercase()) {
                    "DELIVERED" -> MessageStatus.DELIVERED
                    "READ" -> MessageStatus.READ
                    else -> MessageStatus.SENT
                }
            )
        } catch (e: Exception) {
            Log.e("ChatDebug", "Ошибка в fetchLastMessage для ${chatRef.id}: ${e.message}", e)
            Message()
        }
    }

    // 5. Подсчет непрочитанных сообщений
    private suspend fun fetchUnreadCount(chatRef: DocumentReference, otherUserId: String): Int {
        Log.d("ChatDebug", "fetchUnreadCount для чата ${chatRef.id}")
        return try {
            val unreadSnapshot = chatRef.collection("messages")
                .whereEqualTo("userId", otherUserId)
                .whereNotEqualTo("status", "READ")
                .get()
                .await()

            val count = unreadSnapshot.size()
            Log.d("ChatDebug", "Непрочитанных сообщений: $count")
            count
        } catch (e: Exception) {
            Log.e("ChatDebug", "Ошибка в fetchUnreadCount для ${chatRef.id}: ${e.message}", e)
            0
        }
    }


}