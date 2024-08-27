package com.chatapp.chatapp.presentation.screens.Chat

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.domain.MessageRepository
import com.chatapp.chatapp.domain.models.Message
import com.chatapp.chatapp.domain.models.MessageStatus
import com.chatapp.chatapp.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _latestMessages = MutableStateFlow<Map<String, Message>>(emptyMap())
    val latestMessages = _latestMessages.asStateFlow()

    private val _messageCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val messageCounts = _messageCounts.asStateFlow()

    private val _chatIds = MutableStateFlow<List<String>>(emptyList())


    var inputMessage by mutableStateOf("")
        private set
    var isEditing by mutableStateOf(false)
        private set
    var editingMessageId by mutableStateOf("")
        private set
    var newMessageText by mutableStateOf("")
        private set


    fun generateChatId(userJson: String): Pair<String,User> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val userType = object : TypeToken<User>() {}.type
        val otherUser: User = Gson().fromJson(userJson, userType)
        val userId = if (currentUserId < otherUser.userId) "$currentUserId-${otherUser.userId}" else "${otherUser.userId}-$currentUserId"
        return Pair(userId,otherUser)
    }


    fun startListeningToChats() {
        _chatIds.flatMapLatest { chatIds -> messageRepository.listenForMessagesInChats(chatIds) }
            .onEach { chatMessagesMap -> processChatMessages(chatMessagesMap) }
            .launchIn(viewModelScope)
    }

    private fun processChatMessages(chatMessagesMap: Map<String, List<Message>>) {
        _latestMessages.value = chatMessagesMap.mapValues { (_, messages) ->
            messages.maxByOrNull { it.timestamp } ?: defaultMessage()
        }
        _messageCounts.value = chatMessagesMap.mapValues { (_, messages) ->
            messages.count { it.status == MessageStatus.DELIVERED }
        }
    }

    private fun defaultMessage() = Message(
        userId = "",
        text = "",
        timestamp = Date(0),
        messageId = "",
        status = MessageStatus.SENT
    )

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


    fun sendMessage(chatId: String, userName: String, message: String) {
        viewModelScope.launch {
            messageRepository.sendMessage(chatId, userName, message)
        }
    }

    fun markMessageAsRead(chatId: String, messageId: String) {
        viewModelScope.launch {
            messageRepository.markMessageAsRead(chatId, messageId)
        }
    }

    fun getMessages(chatId: String) {
        viewModelScope.launch {
            _messages.value = messageRepository.getMessages(chatId)
        }
    }

    fun listenForMessages(chatId: String) {
        messageRepository.listenForMessages(chatId) { messagesList ->
            _messages.value = messagesList
        }
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