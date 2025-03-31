package com.chatapp.chatapp.features.chat_rooms.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat.domain.MessageStatus
import com.chatapp.chatapp.features.chat_rooms.domain.ChatRoomsRepository
import com.chatapp.chatapp.features.chat_rooms.presentation.ChatRoomsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomsViewModel @Inject constructor(
    private val chatRoomsRepository: ChatRoomsRepository
) : ViewModel() {

    private val _chatRoomsState = MutableStateFlow<List<ChatRoomsState>>(emptyList())
    val chatRoomsState = _chatRoomsState.asStateFlow()

    private val _chatIds = MutableStateFlow<List<String>>(emptyList())
    val chatIds = _chatIds.asStateFlow()

    init {
        startListeningToChats()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startListeningToChats() {
        _chatIds.filter { it.isNotEmpty() }
            .flatMapLatest { chatIds -> chatRoomsRepository.listenForMessagesInChats(chatIds) }
            .onEach { chatMessagesMap -> processChatMessages(chatMessagesMap) }
            .launchIn(viewModelScope)
    }

    private fun processChatMessages(chatMessagesMap: Map<String, List<Message>>) {
        _chatRoomsState.update { currentChatRooms ->
            currentChatRooms.map { chatRoom ->
                val messages = chatMessagesMap[chatRoom.chatId]
                if (messages != null) {
                    val lastMessage = messages.maxByOrNull { it.timestamp } ?: Message()
                    val unreadCount = messages.count { it.status == MessageStatus.DELIVERED }
                    chatRoom.copy(
                        lastMessage = lastMessage,
                        unreadMessageCount = unreadCount
                    )
                } else {
                    chatRoom
                }
            }
        }
    }

    fun loadChatRooms(userId: String, onSucces:(Boolean) -> Unit) {
        viewModelScope.launch {
            chatRoomsRepository.getUserChatRooms(userId)
                .collect { chatRooms ->
                    _chatRoomsState.update { current ->
                        if (current.isEmpty()) chatRooms else current
                    }
                }
            chatRoomsRepository.getAllChatRoomsId(userId)
                .collect { chatIds ->
                    _chatIds.value = chatIds
                }
        }.invokeOnCompletion {
            onSucces(true)
        }
    }

    fun clearChatRooms() {
        _chatRoomsState.value = emptyList()
    }


}