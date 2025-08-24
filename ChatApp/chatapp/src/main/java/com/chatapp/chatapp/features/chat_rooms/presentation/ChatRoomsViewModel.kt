package com.chatapp.chatapp.features.chat_rooms.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat_rooms.domain.ChatRoomsRepository
import com.chatapp.chatapp.features.chat_rooms.domain.models.ChatRoomsMessagesInfo
import com.chatapp.chatapp.features.chat_rooms.domain.models.ChatRooms
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatRoomsViewModel @Inject constructor(
    private val chatRoomsRepository: ChatRoomsRepository
) : ViewModel() {

    private val _chatRooms = MutableStateFlow<List<ChatRooms>>(emptyList())
    val chatRooms = _chatRooms.asStateFlow()

    private val _chatIds = MutableStateFlow<List<String>>(emptyList())
    private var isListening = false

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun loadAndListenToChats(userId: String) {
        return withContext(Dispatchers.IO) {
            if (_chatRooms.value.isNotEmpty() && !isListening) return@withContext

            if (_chatRooms.value.isEmpty()) {
                chatRoomsRepository.getUserChatRooms(userId)
                    .catch { e -> Log.e("ChatViewModel", "Ошибка загрузки чатов: ${e.message}", e) }
                    .collect { chatRooms ->
                        val sortedChatRooms = chatRooms.sortedByDescending { it.lastMessage.timestamp }
                        _chatRooms.value = sortedChatRooms
                        _chatIds.value = sortedChatRooms.map { it.chatId }
                    }
            }

            if (!isListening) {
                _chatIds
                    .filter { it.isNotEmpty() }
                    .flatMapLatest { chatIds ->
                        chatRoomsRepository.lastMessagesListner(chatIds)
                            .map { chatMessagesMap -> processChatMessages(chatMessagesMap) }
                    }
                    .catch { e -> Log.e("ChatViewModel", "Ошибка в слушателе сообщений: ${e.message}", e) }
                    .launchIn(viewModelScope)
                isListening = true
            }
        }
    }


    private fun processChatMessages(chatMessagesMap: Map<String, ChatRoomsMessagesInfo>) {
        _chatRooms.update { currentChatRooms ->
            val updatedChatRooms = currentChatRooms.map { chatRoom ->
                val chatInfo = chatMessagesMap[chatRoom.chatId]
                if (chatInfo != null && chatInfo.lastMessage != null) {
                    chatRoom.copy(
                        lastMessage = chatInfo.lastMessage,
                        unreadMessageCount = chatInfo.unreadMessageCount
                    )
                } else {
                    chatRoom
                }
            }
            updatedChatRooms.sortedByDescending { it.lastMessage.timestamp }
        }
    }

    fun clearChatRooms() {
        _chatRooms.value = emptyList()
    }


}