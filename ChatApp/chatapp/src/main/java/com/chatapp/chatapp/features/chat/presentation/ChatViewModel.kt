package com.chatapp.chatapp.features.chat.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.chat.domain.MessageRepository
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.presentation.details.ChatItem
import com.chatapp.chatapp.features.chat.presentation.details.inputField.ChatInputFieldState
import com.chatapp.chatapp.features.chat.presentation.details.inputField.SendState
import com.chatapp.chatapp.features.chat.presentation.details.topbar.TopMenuState
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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


    /**
     * Редактирование выделенных сообщений в чате
     */
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
        _topMenuState.update { it.copy(listSelectedMessages = emptyList(), countSelectedMessage = 0) }
    }
    fun updateStateTopMenuMessage(value: Boolean) {
        _topMenuState.update { it.copy(isOpenTopMenu = value) }
    }

    fun copySelectedMessage(context: Context,selectedMessages: List<Message>): String{
        if (selectedMessages.isNotEmpty()) {
            val copiedText = selectedMessages.joinToString("\n") { it.text }
            val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Copied Messages",copiedText )
            clipboardManager.setPrimaryClip(clipData)
            resetStateTopMenu()
            return copiedText
        }
        return ""
    }
    fun resetStateTopMenu(){
        _topMenuState.update {
            it.copy(
                listSelectedMessages = emptyList(),
                countSelectedMessage = 0,
                isOpenTopMenu = false
            )
        }
    }



    /**
     * Состояние InputField в чате и методы для сообщений
     */

    private val _chatInputFieldState = MutableStateFlow(ChatInputFieldState())
    val chatInputFieldState = _chatInputFieldState.asStateFlow()

    fun updateDefaultInputMessage(newText: String) {
        _chatInputFieldState.value = _chatInputFieldState.value.copy(defaultInputMessage = newText)
    }
    fun updateEditingInputMessage(newText: String) {
        _chatInputFieldState.value = _chatInputFieldState.value.copy(editingInputMessage = newText)
    }
    fun initEditMessageState(isEditing: Boolean, newMessageText: String, editingMessage: Message?) {
        _chatInputFieldState.value = _chatInputFieldState.value.copy(
            isEditingMessage = isEditing,
            editingInputMessage = newMessageText,
            editingMessage = editingMessage
        )
    }

    fun resetEditMode() {
        _chatInputFieldState.value = _chatInputFieldState.value.copy(
            isEditingMessage = false,
            editingInputMessage = "",
            editingMessage = null
        )
        resetStateTopMenu()
    }
    fun handleSendMessage(
        currentChatId: String,
        currentUserId: String,
        otherUserId: String,
        sendState: SendState
    ) {
        when (sendState) {
            is SendState.SendEditMessage -> {
                if (chatInputFieldState.value.editingInputMessage.isNotEmpty()) {
                    onSaveEditMessage(
                        chatId = currentChatId,
                        messageId = chatInputFieldState.value.editingMessage?.messageId ?: "",
                        newMessageText = chatInputFieldState.value.editingInputMessage
                    )
                    resetEditMode()
                }
            }
            is SendState.SendDefaultMessage -> {
                if (chatInputFieldState.value.defaultInputMessage.isNotEmpty()) {
                    sendMessage(
                        chatId = currentChatId,
                        currentUserId = currentUserId,
                        otherUserId = otherUserId,
                        message = chatInputFieldState.value.defaultInputMessage
                    )
                    resetDefaultInputMessage()
                }
            }
        }
    }
    fun resetDefaultInputMessage() {
        _chatInputFieldState.value = _chatInputFieldState.value.copy(defaultInputMessage = "")
    }
    private fun sendMessage(chatId: String, currentUserId: String, otherUserId: String, message: String) {
        viewModelScope.launch {
            messageRepository.sendMessage(chatId, currentUserId, otherUserId, message)
        }
    }
    private fun onSaveEditMessage(chatId: String, messageId: String, newMessageText: String) {
        viewModelScope.launch {
            messageRepository.onSaveEditMessage(chatId, messageId, newMessageText)
        }
    }
    fun deleteMessage(chatId: String, selectedMessages: List<Message>) {
        viewModelScope.launch {
            messageRepository.deleteMessage(chatId, selectedMessages)
            resetStateTopMenu()
        }
    }
    fun markMessageAsRead(chatId: String, messageId: String,currentUserId: String) {
        viewModelScope.launch {
            messageRepository.markMessageAsRead(chatId, messageId, currentUserId)
        }
    }



    /**
     * Отслеживание колличества непрочитанных сообщений, вне зоне просмотра этих сообщений
     */
    val unreadMessages = mutableListOf<Message>()

    private val _unreadMessagesCount = MutableStateFlow(0)
    val unreadMessagesCount = _unreadMessagesCount.asStateFlow()

    fun resetUnreadMessagesCount() {
        unreadMessages.clear()
        _unreadMessagesCount.value = unreadMessages.size
    }
    fun deleteUnreadMessageToScroll(currentMessage: Message){
        unreadMessages.remove(currentMessage)
        _unreadMessagesCount.value = unreadMessages.size
    }


    /**
     * Слушатели для получение сообщений для всех пользователей [startListeningToChats].
     * Cлушатель для получений сообщений в текущем чате [listenForMessagesInChat].
     * Поиск последних сообщений и их колличество [processChatMessages].
     */
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _chatItems = MutableStateFlow<List<ChatItem>>(emptyList())
    val chatItems = _chatItems.asStateFlow()


    fun listenForMessagesInChat(chatId: String, listState: LazyListState) {
        messageRepository.listenMessagesInCurrentChat(chatId) { newMessages, updatedMessages, removedMessagesIds ->
            _chatItems.update { currentChatItems ->
                val messageItems = currentChatItems.filterIsInstance<ChatItem.MessageItem>().toMutableList()
                messageItems.removeAll { it.message.messageId in removedMessagesIds }
                val updatedMessagesMap = updatedMessages.associateBy { it.messageId }
                messageItems.replaceAll { chatItem ->
                    updatedMessagesMap[chatItem.message.messageId]?.let { ChatItem.MessageItem(it) } ?: chatItem
                }
                messageItems.addAll(0,newMessages.map { ChatItem.MessageItem(it) })
                val sortedMessages = messageItems.map { it.message }
                generateChatItems(sortedMessages)
            }
            if (listState.firstVisibleItemIndex > 1) {
                unreadMessages.addAll(newMessages)
                _unreadMessagesCount.value = unreadMessages.size
            }
        }
    }
    /**
     * Генерация ChatId и ChatItem
     */
    fun generateChatId(otehrUserJson: String, currentUserJson: String): Triple<String, User, User> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val userType = object : TypeToken<User>() {}.type
        val otherUser: User = Gson().fromJson(otehrUserJson, userType)
        val currentUser: User = Gson().fromJson(currentUserJson, userType)
        val chatId = if (currentUserId < otherUser.userId) "$currentUserId-${otherUser.userId}" else "${otherUser.userId}-$currentUserId"
        return Triple(chatId, otherUser, currentUser)
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
}