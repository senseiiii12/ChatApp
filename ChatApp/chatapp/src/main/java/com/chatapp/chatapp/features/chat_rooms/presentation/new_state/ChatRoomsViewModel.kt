package com.chatapp.chatapp.features.chat_rooms.presentation.new_state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.chat_rooms.domain.ChatRoomsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomsViewModel @Inject constructor(
    private val chatRoomsRepository: ChatRoomsRepository
): ViewModel() {

    private val _chatRoomsState = MutableStateFlow<List<ChatRoomsState>>(emptyList())
    val chatRoomsState = _chatRoomsState.asStateFlow()



    fun getChatRoomsId(currentUserId: String){
        viewModelScope.launch {
            chatRoomsRepository.getChatRoomsId(currentUserId).collect { chatRoomsList ->
                _chatRoomsState.value = chatRoomsList
            }
        }
    }
    fun getUserChatRooms(userId: String){
        viewModelScope.launch {
            chatRoomsRepository.getUserChatRooms(userId).collect { chatRoomsList ->
                _chatRoomsState.value = chatRoomsList
            }
        }
    }
}