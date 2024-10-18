package com.chatapp.chatapp.presentation.screens.Chat

import android.util.Log
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.domain.MessageRepository
import com.chatapp.chatapp.domain.models.Message
import com.chatapp.chatapp.domain.models.MessageStatus
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.presentation.screens.Chat.details.ChatItem
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _chatItems = MutableStateFlow<List<ChatItem>>(emptyList())
    val chatItems = _chatItems.asStateFlow()

    private val _latestMessages = MutableStateFlow<Map<String, Message>>(emptyMap())
    val latestMessages = _latestMessages.asStateFlow()

    private val _messageCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val messageCounts = _messageCounts.asStateFlow()

    private val _chatIds = MutableStateFlow<List<String>>(emptyList())

    init {
        Log.d("ViewModel", "init ChatViewModel")
    }

    var inputMessage by mutableStateOf("")
        private set
    var isEditing by mutableStateOf(false)
        private set
    var editingMessageId by mutableStateOf("")
        private set
    var newMessageText by mutableStateOf("")
        private set


    fun generateChatId(otehrUserJson: String, currentUserJson: String): Triple<String, User, User> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val userType = object : TypeToken<User>() {}.type
        val otherUser: User = Gson().fromJson(otehrUserJson, userType)
        val currentUser: User = Gson().fromJson(currentUserJson, userType)
        val chatId = if (currentUserId < otherUser.userId) "$currentUserId-${otherUser.userId}" else "${otherUser.userId}-$currentUserId"
        return Triple(chatId, otherUser, currentUser)
    }


    fun startListeningToChats() {
        _chatIds.flatMapLatest { chatIds -> messageRepository.listenForMessagesInChats(chatIds) }
            .onEach { chatMessagesMap -> processChatMessages(chatMessagesMap) }
            .launchIn(viewModelScope)
    }

    private fun processChatMessages(chatMessagesMap: Map<String, List<Message>>) {
        _latestMessages.value = chatMessagesMap.mapValues { (_, messages) ->
            messages.maxByOrNull { it.timestamp } ?: Message()
        }
        _messageCounts.value = chatMessagesMap.mapValues { (_, messages) ->
            messages.count { it.status == MessageStatus.DELIVERED }
        }
    }

    fun updateChatIds(chatIds: List<String>) {
        _chatIds.value = chatIds
    }

    fun updateInputMessageText(newText: String) {
        inputMessage = newText
    }

    fun updateNewMessageText(newText: String) {
        newMessageText = newText
    }

    fun initEditMessageState(isEditing: Boolean, newMessageText: String, editingMessageId: String) {
        this.isEditing = isEditing
        this.newMessageText = newMessageText
        this.editingMessageId = editingMessageId
    }

    fun resetEditMode() {
        isEditing = false
        editingMessageId = ""
        newMessageText = ""
    }

    fun resetInputMessage() {
        inputMessage = ""
    }


    fun sendMessage(chatId: String, currentUserId: String, message: String) {
        viewModelScope.launch {
            messageRepository.sendMessage(chatId, currentUserId, message)
        }
    }

    fun markMessageAsRead(chatId: String, messageId: String) {
        viewModelScope.launch {
            messageRepository.markMessageAsRead(chatId, messageId)
        }
    }


    fun listenForMessages(chatId: String) {
        messageRepository.listenForMessages(chatId) { newMessages, updatedMessages, removedMessagesIds ->
            _messages.update { currentMessages ->
                val updatedList = currentMessages.filterNot { message ->
                    removedMessagesIds.contains(message.messageId)
                }.toMutableList()

                updatedMessages.forEach { updatedMessage ->
                    val index = updatedList.indexOfFirst { it.messageId == updatedMessage.messageId }
                    if (index != -1) {
                        updatedList[index] = updatedMessage
                    }
                }

                updatedList.addAll(0, newMessages)

                val itemsWithSeparators = generateChatItems(updatedList)
                _chatItems.value = itemsWithSeparators

                updatedList
            }
        }
    }

    fun generateChatItems(messages: List<Message>): List<ChatItem> {
        val chatItems = mutableListOf<ChatItem>()
        var lastMessageDate: String? = null

        for (message in messages.reversed()) {
            val messageDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(message.timestamp))

            if (messageDate != lastMessageDate) {
                chatItems.add(ChatItem.DateSeparatorItem(messageDate))
                lastMessageDate = messageDate
            }

            chatItems.add(ChatItem.MessageItem(message))
        }
        return chatItems.reversed()
    }







    fun deleteMessage(chatId: String, messageId: String) {
        viewModelScope.launch {
            messageRepository.deleteMessage(chatId, messageId)
        }
    }

    fun onSaveEditMessage(chatId: String, messageId: String, newMessageText: String) {
        viewModelScope.launch {
            messageRepository.onSaveEditMessage(chatId, messageId, newMessageText)
        }
    }
}