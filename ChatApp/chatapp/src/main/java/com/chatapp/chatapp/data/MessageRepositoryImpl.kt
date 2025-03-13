package com.chatapp.chatapp.data

import android.util.Log
import com.chatapp.chatapp.domain.MessageRepository
import com.chatapp.chatapp.domain.models.Message
import com.chatapp.chatapp.domain.models.MessageStatus
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SnapshotListenOptions
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

    val options = SnapshotListenOptions.Builder()
        .setSource(ListenSource.DEFAULT)
        .build()

    override suspend fun sendMessage(chatId: String, currentUserId: String, messageText: String) {
        val messageId = UUID.randomUUID().toString()
        val messageMap = mutableMapOf(
            "userId" to currentUserId,
            "text" to messageText,
            "messageId" to messageId,
            "status" to MessageStatus.DELIVERED.name,
            "timestamp" to FieldValue.serverTimestamp()
        )
        try {
            chatCollection.document(chatId)
                .collection("messages")
                .document(messageId)
                .set(messageMap)
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


    override fun listenForMessages(chatId: String, onMessagesChanged: (List<Message>, List<Message>, List<String>) -> Unit) {
        chatCollection.document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener(options) { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener

                val newMessages = snapshot.documentChanges
                    .filter { it.type == DocumentChange.Type.ADDED }
                    .map { it.document.toMessage() }

                val updatedMessages = snapshot.documentChanges
                    .filter { it.type == DocumentChange.Type.MODIFIED }
                    .map { it.document.toMessage() }

                val removedMessagesIds = snapshot.documentChanges
                    .filter { it.type == DocumentChange.Type.REMOVED }
                    .map { it.document.getString("messageId") ?: "" }

                onMessagesChanged(newMessages, updatedMessages, removedMessagesIds)
            }
    }
    private fun DocumentSnapshot.toMessage(): Message {
        return Message(
            userId = getString("userId") ?: "",
            text = getString("text") ?: "",
            timestamp = getTimestamp("timestamp")?.toDate()?.time ?: 0L,
            messageId = getString("messageId") ?: "",
            status = MessageStatus.valueOf(getString("status") ?: MessageStatus.SENT.name)
        )
    }

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
        }
    }

    override suspend fun deleteMessage(chatId: String, selectedMessages: List<Message>) {
        try {
            firestore.runTransaction { transaction ->
                selectedMessages.forEach { message ->
                    val messageRef = chatCollection
                        .document(chatId)
                        .collection("messages")
                        .document(message.messageId)
                    transaction.delete(messageRef)
                }
                null
            }.await()
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Failed to delete messages", e)
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