package com.chatapp.chatapp.features.chat.data

import android.util.Log
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat.domain.MessageChanges
import com.chatapp.chatapp.features.chat.domain.MessageRepository
import com.chatapp.chatapp.features.chat.domain.MessageStatus
import com.chatapp.chatapp.util.extension.toMessage
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MessageRepository {

    private val chatCollection = firestore.collection(COLLECTION_CHATS)

    companion object {
        private const val COLLECTION_CHATS = "chats"
        private const val COLLECTION_MESSAGES = "messages"
        private const val FIELD_USER_ID = "userId"
        private const val FIELD_TEXT = "text"
        private const val FIELD_MESSAGE_ID = "messageId"
        private const val FIELD_STATUS = "status"
        private const val FIELD_TIMESTAMP = "timestamp"
        private const val FIELD_PARTICIPANTS = "participants"
        private const val FIELD_CHAT_ID = "chatId"
        private const val TAG = "MessageRepository"
    }

    /**
     * Получить ссылку на документ сообщения
     */
    private fun getMessageRef(chatId: String, messageId: String) =
        chatCollection.document(chatId)
            .collection(COLLECTION_MESSAGES)
            .document(messageId)

    /**
     * Получить ссылку на коллекцию сообщений
     */
    private fun getMessagesCollection(chatId: String) =
        chatCollection.document(chatId)
            .collection(COLLECTION_MESSAGES)

    /**
     * Отправить сообщение
     */
    override suspend fun sendMessage(
        chatId: String,
        currentUserId: String,
        messageText: String
    ): Result<Unit> {
        return try {
            require(messageText.isNotBlank()) { "Message text cannot be empty" }
            require(chatId.isNotBlank()) { "Chat ID cannot be empty" }
            require(currentUserId.isNotBlank()) { "User ID cannot be empty" }

            val messageId = UUID.randomUUID().toString()

            val messageMap = hashMapOf(
                FIELD_USER_ID to currentUserId,
                FIELD_TEXT to messageText,
                FIELD_MESSAGE_ID to messageId,
                FIELD_STATUS to MessageStatus.DELIVERED.name,
                FIELD_TIMESTAMP to FieldValue.serverTimestamp()
            )

            val participantsMap = hashMapOf(
                FIELD_PARTICIPANTS to chatId.split("-"),
                FIELD_CHAT_ID to chatId
            )

            firestore.runBatch { batch ->
                val messageRef = getMessageRef(chatId, messageId)
                batch.set(messageRef, messageMap)

                val chatRef = chatCollection.document(chatId)
                batch.set(chatRef, participantsMap, SetOptions.merge())
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message", e)
            Result.failure(e)
        }
    }

    /**
     * Пометить сообщение как прочитанное
     */
    override suspend fun markMessageAsRead(chatId: String, messageId: String): Result<Unit> {
        return try {
            require(chatId.isNotBlank()) { "Chat ID cannot be empty" }
            require(messageId.isNotBlank()) { "Message ID cannot be empty" }

            getMessageRef(chatId, messageId)
                .update(FIELD_STATUS, MessageStatus.READ.name)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark message as read: $messageId", e)
            Result.failure(e)
        }
    }

    /**
     * Слушать изменения сообщений в чате (callback версия)
     */
    override fun listenMessagesInCurrentChat(
        chatId: String,
        onMessagesChanged: (List<Message>, List<Message>, List<String>) -> Unit,
        onError: ((Exception) -> Unit)?
    ): ListenerRegistration {
        return getMessagesCollection(chatId)
            .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e(TAG, "Error listening to messages", e)
                    onError?.invoke(e)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val newMessages = snapshot.documentChanges
                    .filter { it.type == DocumentChange.Type.ADDED }
                    .mapNotNull {
                        try {
                            it.document.toMessage()
                        } catch (ex: Exception) {
                            Log.e(TAG, "Error converting message", ex)
                            null
                        }
                    }

                val updatedMessages = snapshot.documentChanges
                    .filter { it.type == DocumentChange.Type.MODIFIED }
                    .mapNotNull {
                        try {
                            it.document.toMessage()
                        } catch (ex: Exception) {
                            Log.e(TAG, "Error converting message", ex)
                            null
                        }
                    }

                val removedMessagesIds = snapshot.documentChanges
                    .filter { it.type == DocumentChange.Type.REMOVED }
                    .mapNotNull { it.document.getString(FIELD_MESSAGE_ID) }

                onMessagesChanged(newMessages, updatedMessages, removedMessagesIds)
            }
    }

    override fun listenMessagesFlow(chatId: String): Flow<MessageChanges> {
        return callbackFlow {
            val listener = getMessagesCollection(chatId)
                .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e(TAG, "Error in message flow", e)
                        close(e)
                        return@addSnapshotListener
                    }

                    if (snapshot == null) return@addSnapshotListener

                    val newMessages = snapshot.documentChanges
                        .filter { it.type == DocumentChange.Type.ADDED }
                        .mapNotNull {
                            try {
                                it.document.toMessage()
                            } catch (ex: Exception) {
                                Log.e(TAG, "Error converting message", ex)
                                null
                            }
                        }

                    val updatedMessages = snapshot.documentChanges
                        .filter { it.type == DocumentChange.Type.MODIFIED }
                        .mapNotNull {
                            try {
                                it.document.toMessage()
                            } catch (ex: Exception) {
                                Log.e(TAG, "Error converting message", ex)
                                null
                            }
                        }

                    val removedMessagesIds = snapshot.documentChanges
                        .filter { it.type == DocumentChange.Type.REMOVED }
                        .mapNotNull { it.document.getString(FIELD_MESSAGE_ID) }

                    trySend(MessageChanges(newMessages, updatedMessages, removedMessagesIds))
                }

            awaitClose { listener.remove() }
        }
    }

    /**
     * Слушать изменения сообщений в чате (Flow версия)
     */


    /**
     * Удалить сообщения
     */
    override suspend fun deleteMessage(
        chatId: String,
        selectedMessages: List<Message>
    ): Result<Unit> {
        return try {
            require(chatId.isNotBlank()) { "Chat ID cannot be empty" }
            require(selectedMessages.isNotEmpty()) { "No messages to delete" }

            firestore.runBatch { batch ->
                selectedMessages.forEach { message ->
                    val messageRef = getMessageRef(chatId, message.messageId)
                    batch.delete(messageRef)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete messages", e)
            Result.failure(e)
        }
    }

    /**
     * Сохранить отредактированное сообщение
     */
    override suspend fun onSaveEditMessage(
        chatId: String,
        messageId: String,
        newMessageText: String
    ): Result<Unit> {
        return try {
            require(chatId.isNotBlank()) { "Chat ID cannot be empty" }
            require(messageId.isNotBlank()) { "Message ID cannot be empty" }
            require(newMessageText.isNotBlank()) { "Message text cannot be empty" }

            val updatedMessage = hashMapOf<String, Any>(
                FIELD_TEXT to newMessageText
            )

            getMessageRef(chatId, messageId)
                .update(updatedMessage)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update message: $messageId", e)
            Result.failure(e)
        }
    }
}

/**
 * Data class для Flow версии слушателя сообщений
 */
data class MessageChanges(
    val newMessages: List<Message>,
    val updatedMessages: List<Message>,
    val removedMessagesIds: List<String>
)