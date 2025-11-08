package com.chatapp.chatapp.features.chat_rooms.data

import android.util.Log
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat.domain.MessageStatus
import com.chatapp.chatapp.features.chat_rooms.domain.ChatRoomsRepository
import com.chatapp.chatapp.features.chat_rooms.domain.models.ChatRoomsMessagesInfo
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
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRoomsRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
) : ChatRoomsRepository {

    val chatCollection = firebaseFirestore.collection("chats")

//    override suspend fun lastMessagesListner(chatIds: List<String>): Flow<Map<String, ChatRoomsMessagesInfo>> {
//        return callbackFlow {
//            val currentMessages = mutableMapOf<String, ChatRoomsMessagesInfo>().withDefault {
//                ChatRoomsMessagesInfo(Message(), 0)
//            }
//            val listeners = chatIds.map { chatId ->
//                chatCollection.document(chatId)
//                    .collection("messages")
//                    .orderBy("timestamp", Query.Direction.DESCENDING)
//                    .limit(20)
//                    .addSnapshotListener { snapshot, e ->
//                        if (e != null || snapshot == null) return@addSnapshotListener
//                        val messages = snapshot.documents.map { it.toMessage() }
//                        val lastMessage = messages.maxByOrNull { it.timestamp } ?: Message()
//                        val unreadMessageCount = messages.count { it.status == MessageStatus.DELIVERED }
//
//                        currentMessages[chatId] = ChatRoomsMessagesInfo(
//                            lastMessage = lastMessage,
//                            unreadMessageCount = unreadMessageCount
//                        )
//                        trySend(currentMessages.toMap())
//                    }
//            }
//            awaitClose { listeners.forEach { it.remove() } }
//        }.flowOn(Dispatchers.IO)
//    }

    override suspend fun lastMessagesListner(
        chatIds: List<String>,
        currentUserId: String
    ): Flow<Map<String, ChatRoomsMessagesInfo>> = callbackFlow {

        val resultMap = mutableMapOf<String, ChatRoomsMessagesInfo>().apply {
            chatIds.forEach { put(it, ChatRoomsMessagesInfo()) }
        }

        val listeners = mutableListOf<ListenerRegistration>()

        chatIds.forEach { chatId ->
            // 1. Слушатель последнего сообщения
            val msgListener = chatCollection.document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener { snapshot, _ ->
                    val lastMessage = snapshot?.documents?.firstOrNull()?.toMessage() ?: Message()
                    resultMap[chatId] = (resultMap[chatId] ?: ChatRoomsMessagesInfo()).copy(lastMessage = lastMessage)
                    trySend(resultMap.toMap())
                }
            listeners.add(msgListener)

            // 2. Слушатель unreadCounters
            val unreadListener = chatCollection.document(chatId)
                .addSnapshotListener { snapshot, _ ->
                    val unreadCounters = snapshot?.get("unreadCounters") as? Map<String, Long> ?: emptyMap()
                    val unreadCount = unreadCounters[currentUserId]?.toInt() ?: 0
                    resultMap[chatId] = (resultMap[chatId] ?: ChatRoomsMessagesInfo()).copy(unreadMessageCount = unreadCount)
                    trySend(resultMap.toMap())
                }
            listeners.add(unreadListener)
        }
        awaitClose {
            listeners.forEach { it.remove() }
        }
    }.flowOn(Dispatchers.IO)


    override suspend fun getAllChatRoomsId(currentUserId: String): Flow<List<String>> {
        return flow {
            try {
                val result = firebaseFirestore.collection("chats")
                    .whereArrayContains("participants", currentUserId)
                    .get()
                    .await()
                val chatIds = result.map { it.id }
                emit(chatIds)
            } catch (e: Exception) {
                emit(emptyList())
                Log.d("getChatRoomsId", e.message.toString())
            }
        }
    }

    override suspend fun getUserChatRooms(userId: String): Flow<List<ChatRooms>> {
        return flow {
            val chatIdsFlow: Flow<List<String>> = flow {
                val chatsSnapshot = firebaseFirestore.collection("chats")
                    .whereArrayContains("participants", userId)
                    .get()
                    .await()
                val chatIds = chatsSnapshot.documents.map { it.id }
                emit(chatIds)
            }

            emitAll(
                chatIdsFlow.flatMapLatest { chatIds: List<String> ->
                    if (chatIds.isEmpty()) {
                        flowOf(emptyList<ChatRooms>())
                    } else {
                        flow {
                            val chatRooms: List<ChatRooms> = chatIds.map { chatId ->
                                flow<ChatRooms?> {
                                    emit(buildChatRoomState(chatId, userId))
                                }.catch { e ->
                                    Log.e("ChatDebug", "Ошибка для чата $chatId: ${e.message}", e)
                                    emit(null)
                                }
                            }.merge()
                                .filterNotNull()
                                .toList()
                            emit(chatRooms)
                        }
                    }
                }
            )
        }.flowOn(Dispatchers.IO)
    }




    private suspend fun buildChatRoomState(chatId: String, currentUserId: String): ChatRooms? {
        return try {
            val chatDoc = firebaseFirestore.collection("chats")
                .document(chatId)
                .get()
                .await()

            if (!chatDoc.exists()) return null
            val participants = chatDoc.get("participants") as? List<String> ?: emptyList()
            val otherUserId = participants.firstOrNull { it != currentUserId } ?: return null
            val otherUser = fetchUserData(otherUserId) ?: return null

            ChatRooms(
                chatId = chatId,
                otherUser = otherUser,
                isOnline = otherUser.online,
                lastMessage = Message(),
                unreadMessageCount = 0
            )
        } catch (e: Exception) {
            Log.e("ChatDebug", "Ошибка в buildChatRoomState для $chatId: ${e.message}", e)
            null
        }
    }

    private suspend fun fetchUserData(userId: String): User? {
        return try {
            val userDoc = firebaseFirestore.collection("users")
                .document(userId)
                .get()
                .await()
            if (userDoc.exists()) userDoc.toUser() else null
        } catch (e: Exception) {
            Log.e("ChatDebug", "Ошибка загрузки пользователя $userId: ${e.message}", e)
            null
        }
    }

}