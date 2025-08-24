package com.chatapp.chatapp.features.chat.data

import android.util.Log
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat.domain.MessageRepository
import com.chatapp.chatapp.features.chat.domain.MessageStatus
import com.chatapp.chatapp.util.extension.toMessage
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenSource
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SnapshotListenOptions
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.persistentCacheSettings
import kotlinx.coroutines.tasks.await
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
        val participantsMap = mutableMapOf(
            "participants" to chatId.split("-"),
            "chatId" to chatId
        )
        try {
            chatCollection.document(chatId)
                .collection("messages")
                .document(messageId)
                .set(messageMap)
                .await()
            chatCollection.document(chatId).set(participantsMap).await()
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

    override fun listenMessagesInCurrentChat(chatId: String, onMessagesChanged: (List<Message>, List<Message>, List<String>) -> Unit) {
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