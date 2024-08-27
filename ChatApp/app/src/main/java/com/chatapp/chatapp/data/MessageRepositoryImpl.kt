package com.chatapp.chatapp.data

import android.util.Log
import com.chatapp.chatapp.domain.MessageRepository
import com.chatapp.chatapp.domain.models.Message
import com.chatapp.chatapp.domain.models.MessageStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MessageRepository {

    val chatCollection = firestore.collection("chats")

    override suspend fun sendMessage(chatId: String, userId: String, messageText: String) {
        val messageId = UUID.randomUUID().toString()
        val messageMap = mutableMapOf<String, Any>(
            "userId" to userId,
            "text" to messageText,
            "messageId" to messageId,
            "status" to MessageStatus.SENT.name,
            "timestamp" to FieldValue.serverTimestamp()
        )
        try {
            chatCollection.document(chatId)
                .collection("messages")
                .document(messageId)
                .set(messageMap)
                .await()

            chatCollection.document(chatId)
                .collection("messages")
                .document(messageId)
                .update("status", MessageStatus.DELIVERED.name)
                .await()
        }catch (e:Exception){
            Log.e("ChatViewModel", "Send message: $messageId", e)
        }
    }

    override suspend fun markMessageAsRead(chatId: String, messageId: String) {
        try {
            chatCollection.document(chatId)
                .collection("messages")
                .document(messageId)
                .update("status", MessageStatus.READ.name)
                .await()
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Failed to mark message as read: $messageId", e)
        }
    }

    override suspend fun getMessages(chatId: String): List<Message> {
        return try {
            val result = chatCollection.document(chatId)
                .collection("messages")
                .orderBy("timestamp",Query.Direction.DESCENDING)
                .get().await()
            result.map { document ->
                Message(
                    userId = document.getString("userId") ?: "",
                    text = document.getString("text") ?: "",
                    timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date(0),
                    messageId = document.getString("messageId") ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun listenForMessages(chatId: String, onMessagesChanged: (List<Message>) -> Unit) {
        chatCollection.document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) {
                    return@addSnapshotListener
                }
                val messagesList = snapshot.documents.map { document ->
                    Message(
                        userId = document.getString("userId") ?: "",
                        text = document.getString("text") ?: "",
                        timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date(0),
                        messageId = document.getString("messageId") ?: "",
                        status = MessageStatus.valueOf(document.getString("status") ?: MessageStatus.SENT.name)
                    )
                }
                onMessagesChanged(messagesList)
            }
    }

    override suspend fun listenForMessagesInChats(chatIds: List<String>): Flow<Map<String, List<Message>>> {
        return callbackFlow {
            val currentMessages = mutableMapOf<String, List<Message>>()

            val listeners = chatIds.map { chatId ->
                chatCollection.document(chatId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null || snapshot == null) {
                            // Обработка ошибки
                            return@addSnapshotListener
                        }

                        val messagesList = snapshot.documents.map { document ->
                            Message(
                                userId = document.getString("userId") ?: "",
                                text = document.getString("text") ?: "",
                                timestamp = document.getTimestamp("timestamp")?.toDate() ?: Date(0),
                                messageId = document.getString("messageId") ?: "",
                                status = MessageStatus.valueOf(document.getString("status") ?: MessageStatus.SENT.name)
                            )
                        }
                        currentMessages[chatId] = messagesList
                        trySend(currentMessages.toMap())
                    }
            }
            awaitClose {
                listeners.forEach { listener ->
                    listener.remove()
                }
            }
        }
    }

    override suspend fun deleteMessage(chatId: String, messageId: String) {
        try {
            chatCollection.document(chatId)
                .collection("messages")
                .document(messageId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Failed to delete message: $messageId", e)
        }
    }

    override suspend fun onSaveEditMessage(chatId: String, messageId: String, newMessageText: String) {
        val updatedMessage = mapOf(
            "text" to newMessageText
        )
        try {
            chatCollection.document(chatId)
                .collection("messages")
                .document(messageId)
                .update(updatedMessage)
                .await()
        }catch (e:Exception){
            Log.e("ChatViewModel", "Update message: $messageId", e)
        }
    }

}