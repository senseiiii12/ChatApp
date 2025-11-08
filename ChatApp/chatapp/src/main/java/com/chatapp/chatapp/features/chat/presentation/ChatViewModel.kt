package com.chatapp.chatapp.features.chat.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat.domain.MessageRepository
import com.chatapp.chatapp.features.chat.presentation.details.ChatItem
import com.chatapp.chatapp.features.chat.presentation.details.inputField.ChatInputFieldState
import com.chatapp.chatapp.features.chat.presentation.details.inputField.SendState
import com.chatapp.chatapp.features.chat.presentation.details.topbar.TopMenuState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
        private const val DATE_FORMAT = "dd MMM yyyy"
        private const val CLIPBOARD_LABEL = "Copied Messages"
        private const val MIN_VISIBLE_INDEX_FOR_UNREAD = 1
    }

    private var messageListener: ListenerRegistration? = null

    // ============================================
    // Редактирование выделенных сообщений в чате
    // ============================================

    private val _topMenuState = MutableStateFlow(TopMenuState())
    val topMenuState = _topMenuState.asStateFlow()

    fun toggleMessageSelection(message: Message) {
        _topMenuState.update { currentState ->
            val newList = if (message in currentState.listSelectedMessages) {
                currentState.listSelectedMessages - message
            } else {
                currentState.listSelectedMessages + message
            }
            currentState.copy(
                listSelectedMessages = newList,
                countSelectedMessage = newList.size,
                isOpenTopMenu = newList.isNotEmpty()
            )
        }
    }

    fun clearSelectedMessages() {
        _topMenuState.update {
            it.copy(
                listSelectedMessages = emptyList(),
                countSelectedMessage = 0
            )
        }
    }

    fun stateTopMenuMessage(value: Boolean) {
        _topMenuState.update { it.copy(isOpenTopMenu = value) }
    }

    fun copySelectedMessage(context: Context, selectedMessages: List<Message>): String {
        if (selectedMessages.isEmpty()) return ""

        val copiedText = selectedMessages.joinToString("\n") { it.text }
        val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager

        clipboardManager?.let {
            val clipData = ClipData.newPlainText(CLIPBOARD_LABEL, copiedText)
            it.setPrimaryClip(clipData)
        }

        resetStateTopMenu()
        return copiedText
    }

    fun resetStateTopMenu() {
        _topMenuState.update {
            it.copy(
                listSelectedMessages = emptyList(),
                countSelectedMessage = 0,
                isOpenTopMenu = false
            )
        }
    }

    // ============================================
    // Состояние InputField в чате и методы для сообщений
    // ============================================

    private val _chatInputFieldState = MutableStateFlow(ChatInputFieldState())
    val chatInputFieldState = _chatInputFieldState.asStateFlow()

    private val _operationError = MutableStateFlow<String?>(null)
    val operationError = _operationError.asStateFlow()

    fun updateDefaultInputMessage(newText: String) {
        _chatInputFieldState.update { it.copy(defaultInputMessage = newText) }
    }

    fun updateEditingInputMessage(newText: String) {
        _chatInputFieldState.update { it.copy(editingInputMessage = newText) }
    }

    fun initEditMessageState(isEditing: Boolean, newMessageText: String, editingMessage: Message?) {
        _chatInputFieldState.update {
            it.copy(
                isEditingMessage = isEditing,
                editingInputMessage = newMessageText,
                editingMessage = editingMessage
            )
        }
    }

    fun resetEditMode() {
        _chatInputFieldState.update {
            it.copy(
                isEditingMessage = false,
                editingInputMessage = "",
                editingMessage = null
            )
        }
        resetStateTopMenu()
    }

    fun handleSendMessage(
        currentChatId: String,
        currentUserId: String,
        sendState: SendState
    ) {
        when (sendState) {
            is SendState.SendEditMessage -> {
                val editingMessage = chatInputFieldState.value.editingMessage
                val editingText = chatInputFieldState.value.editingInputMessage

                if (editingText.isNotEmpty() && editingMessage != null) {
                    onSaveEditMessage(
                        chatId = currentChatId,
                        messageId = editingMessage.messageId,
                        newMessageText = editingText
                    )
                    resetEditMode()
                }
            }
            is SendState.SendDefaultMessage -> {
                val messageText = chatInputFieldState.value.defaultInputMessage

                if (messageText.isNotEmpty()) {
                    sendMessage(
                        chatId = currentChatId,
                        currentUserId = currentUserId,
                        message = messageText
                    )
                    resetDefaultInputMessage()
                }
            }
        }
    }

    fun resetDefaultInputMessage() {
        _chatInputFieldState.update { it.copy(defaultInputMessage = "") }
    }

    fun clearError() {
        _operationError.value = null
    }

    private fun sendMessage(chatId: String, currentUserId: String, message: String) {
        viewModelScope.launch {
            messageRepository.sendMessage(chatId, currentUserId, message)
                .onFailure { error ->
                    Log.e(TAG, "Failed to send message", error)
                    _operationError.value = "Не удалось отправить сообщение: ${error.message}"
                }
        }
    }

    private fun onSaveEditMessage(chatId: String, messageId: String, newMessageText: String) {
        viewModelScope.launch {
            messageRepository.onSaveEditMessage(chatId, messageId, newMessageText)
                .onFailure { error ->
                    Log.e(TAG, "Failed to edit message", error)
                    _operationError.value = "Не удалось изменить сообщение: ${error.message}"
                }
        }
    }

    fun deleteMessage(chatId: String, selectedMessages: List<Message>) {
        viewModelScope.launch {
            messageRepository.deleteMessage(chatId, selectedMessages)
                .onSuccess {
                    resetStateTopMenu()
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to delete messages", error)
                    _operationError.value = "Не удалось удалить сообщения: ${error.message}"
                }
        }
    }

    fun markMessageAsRead(chatId: String, messageId: String) {
        viewModelScope.launch {
            messageRepository.markMessageAsRead(chatId, messageId)
                .onFailure { error ->
                    Log.e(TAG, "Failed to mark message as read", error)
                }
        }
    }

    // ============================================
    // Отслеживание количества непрочитанных сообщений
    // ============================================

    private val unreadMessages = mutableListOf<Message>()
    private val _unreadMessagesCount = MutableStateFlow(0)
    val unreadMessagesCount = _unreadMessagesCount.asStateFlow()

    fun resetUnreadMessagesCount() {
        unreadMessages.clear()
        _unreadMessagesCount.value = 0
    }

    fun deleteUnreadMessageToScroll(currentMessage: Message) {
        unreadMessages.remove(currentMessage)
        _unreadMessagesCount.value = unreadMessages.size
    }

    // ============================================
    // Слушатели сообщений
    // ============================================

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _chatItems = MutableStateFlow<List<ChatItem>>(emptyList())
    val chatItems = _chatItems.asStateFlow()

    fun listenForMessagesInChat(chatId: String, listState: LazyListState) {
        // Отписываемся от предыдущего слушателя если он был
        stopListeningToMessages()

        messageListener = messageRepository.listenMessagesInCurrentChat(
            chatId = chatId,
            onMessagesChanged = { newMessages, updatedMessages, removedMessagesIds ->
                handleMessagesChanged(newMessages, updatedMessages, removedMessagesIds, listState)
            },
            onError = { error ->
                Log.e(TAG, "Error listening to messages", error)
                _operationError.value = "Ошибка получения сообщений: ${error.message}"
            }
        )
    }

    private fun handleMessagesChanged(
        newMessages: List<Message>,
        updatedMessages: List<Message>,
        removedMessagesIds: List<String>,
        listState: LazyListState
    ) {
        _chatItems.update { currentChatItems ->
            val messageItems = currentChatItems
                .filterIsInstance<ChatItem.MessageItem>()
                .toMutableList()

            // Удаляем удаленные сообщения
            messageItems.removeAll { it.message.messageId in removedMessagesIds }

            // Обновляем измененные сообщения
            val updatedMessagesMap = updatedMessages.associateBy { it.messageId }
            messageItems.replaceAll { chatItem ->
                updatedMessagesMap[chatItem.message.messageId]?.let {
                    ChatItem.MessageItem(it)
                } ?: chatItem
            }

            // Добавляем новые сообщения в начало
            messageItems.addAll(0, newMessages.map { ChatItem.MessageItem(it) })

            // Генерируем ChatItems с разделителями дат
            val sortedMessages = messageItems.map { it.message }
            generateChatItems(sortedMessages)
        }

        // Обновляем счетчик непрочитанных сообщений
        if (listState.firstVisibleItemIndex > MIN_VISIBLE_INDEX_FOR_UNREAD) {
            unreadMessages.addAll(newMessages)
            _unreadMessagesCount.value = unreadMessages.size
        }
    }

    fun stopListeningToMessages() {
        messageListener?.remove()
        messageListener = null
    }

    // ============================================
    // Генерация ChatId и ChatItem
    // ============================================

    fun generateChatId(otherUserJson: String, currentUserJson: String): Triple<String, User, User> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

        val userType = object : TypeToken<User>() {}.type
        val otherUser: User = Gson().fromJson(otherUserJson, userType)
        val currentUser: User = Gson().fromJson(currentUserJson, userType)

        val chatId = if (currentUserId < otherUser.userId) {
            "$currentUserId-${otherUser.userId}"
        } else {
            "${otherUser.userId}-$currentUserId"
        }

        return Triple(chatId, otherUser, currentUser)
    }

    fun generateChatItems(messages: List<Message>): List<ChatItem> {
        if (messages.isEmpty()) return emptyList()

        val chatItems = mutableListOf<ChatItem>()
        var lastMessageDate: String? = null
        val dateFormatter = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

        for (message in messages.reversed()) {
            val messageDate = try {
                dateFormatter.format(Date(message.timestamp))
            } catch (e: Exception) {
                Log.e(TAG, "Error formatting date", e)
                "Unknown date"
            }

            if (messageDate != lastMessageDate) {
                chatItems.add(ChatItem.DateSeparatorItem(messageDate))
                lastMessageDate = messageDate
            }

            chatItems.add(ChatItem.MessageItem(message))
        }

        return chatItems.reversed()
    }

    // ============================================
    // Очистка ресурсов
    // ============================================

    override fun onCleared() {
        super.onCleared()
        stopListeningToMessages()
    }
}