package com.chatapp.chatapp.features.chat_rooms.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.chat_rooms.domain.ChatRoomsRepository
import com.chatapp.chatapp.features.chat_rooms.domain.models.ChatRooms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomsViewModel @Inject constructor(
    private val chatRoomsRepository: ChatRoomsRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ChatRoomsViewModel"
    }

    private val _chatRooms = MutableStateFlow<List<ChatRooms>>(emptyList())
    val chatRooms = _chatRooms.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    private var chatRoomsJob: Job? = null

    /**
     * Загрузить и слушать все чаты пользователя
     * Все данные (последнее сообщение, непрочитанные, онлайн) обновляются автоматически
     */
    fun loadAndListenToChats(currentUserId: String) {
        if (_currentUserId.value == currentUserId && chatRoomsJob?.isActive == true) {
            Log.d(TAG, "Already listening to chats for user: $currentUserId")
            return
        }

        if (_currentUserId.value != currentUserId) {
            Log.d(TAG, "User changed from ${_currentUserId.value} to $currentUserId, restarting listeners")
            stopListening()
            _currentUserId.value = currentUserId
            _chatRooms.value = emptyList()
        }

        startListening(currentUserId)
    }

    /**
     * Начать слушать чаты
     */
    private fun startListening(currentUserId: String) {
        if (_chatRooms.value.isEmpty()) {
            _isLoading.value = true
        }
        _error.value = null

        chatRoomsJob = viewModelScope.launch {
            chatRoomsRepository.getUserChatRooms(currentUserId)
                .onEach { chatRoomsList ->
                    val sortedChats = chatRoomsList.sortedByDescending {
                        it.lastMessage.timestamp
                    }

                    _chatRooms.value = sortedChats
                    _isLoading.value = false

                    Log.d(TAG, "Updated chat rooms: ${sortedChats.size} chats")
                }
                .catch { exception ->
                    Log.e(TAG, "Error loading chat rooms", exception)
                    _error.value = "Не удалось загрузить чаты: ${exception.message}"
                    _isLoading.value = false
                }
                .launchIn(this)
        }
    }

    /**
     * Остановить слушатели чатов
     */
    fun stopListening() {
        chatRoomsJob?.cancel()
        chatRoomsJob = null
        Log.d(TAG, "Stopped listening to chat rooms")
    }

    /**
     * Очистить список чатов
     */
    fun clearChatRooms() {
        _chatRooms.value = emptyList()
        _currentUserId.value = null
        Log.d(TAG, "Cleared chat rooms")
    }

    /**
     * Сбросить ошибку
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Перезагрузить чаты
     */
    fun reloadChats() {
        val userId = _currentUserId.value
        if (userId != null) {
            stopListening()
            startListening(userId)
        } else {
            Log.w(TAG, "Cannot reload chats: user ID is null")
        }
    }

    /**
     * Получить чат по ID
     */
    fun getChatById(chatId: String): ChatRooms? {
        return _chatRooms.value.find { it.chatId == chatId }
    }

    /**
     * Получить количество чатов с непрочитанными сообщениями
     */
    fun getUnreadChatsCount(): Int {
        return _chatRooms.value.count { it.unreadMessageCount > 0 }
    }

    /**
     * Получить общее количество непрочитанных сообщений
     */
    fun getTotalUnreadCount(): Int {
        return _chatRooms.value.sumOf { it.unreadMessageCount }
    }

    /**
     * Фильтровать чаты по имени собеседника
     */
    fun searchChats(query: String): List<ChatRooms> {
        if (query.isBlank()) return _chatRooms.value

        return _chatRooms.value.filter { chatRoom ->
            chatRoom.otherUser.name.contains(query, ignoreCase = true) ||
                    chatRoom.otherUser.email.contains(query, ignoreCase = true)
        }
    }

    /**
     * Получить только чаты с непрочитанными сообщениями
     */
    fun getUnreadChats(): List<ChatRooms> {
        return _chatRooms.value.filter { it.unreadMessageCount > 0 }
    }

    /**
     * Получить только чаты с онлайн собеседниками
     */
    fun getOnlineChats(): List<ChatRooms> {
        return _chatRooms.value.filter { it.isOnline }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
        Log.d(TAG, "ViewModel cleared")
    }
}