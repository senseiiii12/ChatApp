package com.chatapp.chatapp.features.chat_rooms.data

import android.util.Log
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat_rooms.domain.ChatRoomsRepository
import com.chatapp.chatapp.features.chat_rooms.presentation.ChatRoomsState
import com.chatapp.chatapp.util.extension.toMessage
import com.chatapp.chatapp.util.extension.toUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRoomsRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
) : ChatRoomsRepository {

    val chatCollection = firebaseFirestore.collection("chats")

    override suspend fun listenForMessagesInChats(chatIds: List<String>): Flow<Map<String, List<Message>>> {
        return callbackFlow {
            val currentMessages = mutableMapOf<String, List<Message>>().withDefault { emptyList() }
            val listeners = chatIds.map { chatId ->
                chatCollection.document(chatId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(20)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null || snapshot == null) return@addSnapshotListener
                        val messages = snapshot.documents.map { it.toMessage() }
                        currentMessages[chatId] = messages
                        trySend(currentMessages.toMap())
                    }
            }
            awaitClose { listeners.forEach { it.remove() } }
        }.flowOn(Dispatchers.IO)
    }

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

    override suspend fun getUserChatRooms(userId: String): Flow<List<ChatRoomsState>> {
        return flow {
            try {
                val chatRooms = fetchUserChats(userId)
                emit(chatRooms)
            } catch (e: Exception) {
                emit(emptyList())
            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun fetchUserChats(userId: String): List<ChatRoomsState> {
        val chatsSnapshot = firebaseFirestore.collection("chats")
            .whereArrayContains("participants", userId)
            .get()
            .await()

        if (chatsSnapshot.isEmpty) {
            return emptyList()
        }

        return chatsSnapshot.documents.mapNotNull { chatDoc ->
            buildChatRoomState(chatDoc, userId)
        }
    }

    private suspend fun buildChatRoomState(chatDoc: DocumentSnapshot, currentUserId: String): ChatRoomsState? {
        return try {
            val participants = chatDoc.get("participants") as? List<String> ?: emptyList()
            val otherUserId = participants.firstOrNull { it != currentUserId } ?: return null

            val otherUser = fetchUserData(otherUserId) ?: return null

            ChatRoomsState(
                chatId = chatDoc.id,
                otherUser = otherUser,
                isOnline = otherUser.online,
                lastMessage = Message(),
                unreadMessageCount = 0
            )
        } catch (e: Exception) {
            Log.e("ChatDebug", "Ошибка в buildChatRoomState для ${chatDoc.id}: ${e.message}", e)
            null
        }
    }

    private suspend fun fetchUserData(userId: String): User? {
        return try {
            val userDoc = firebaseFirestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (!userDoc.exists()) {
                return null
            }
            userDoc.toUser()
        } catch (e: Exception) {
            Log.e("ChatDebug", "Ошибка в fetchUserData для $userId: ${e.message}", e)
            null
        }
    }

}