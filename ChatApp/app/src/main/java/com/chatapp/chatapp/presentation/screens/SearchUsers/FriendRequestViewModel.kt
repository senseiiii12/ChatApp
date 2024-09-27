package com.chatapp.chatapp.presentation.screens.SearchUsers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.domain.FriendRequestRepository
import com.chatapp.chatapp.domain.models.FriendRequest
import com.chatapp.chatapp.domain.models.FriendRequestWithUser
import com.chatapp.chatapp.domain.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val friendRequestRepository: FriendRequestRepository
): ViewModel() {

    private val _friendRequestAndUserInfo = MutableStateFlow<List<FriendRequestWithUser>>(emptyList())
    val friendRequestAndUserInfo = _friendRequestAndUserInfo.asStateFlow()

    fun sendFriendRequest(toUserId: String,onResult: (Boolean) -> Unit){
        viewModelScope.launch {
            friendRequestRepository.sendFriendRequest(toUserId){
                onResult(it)
            }
        }
    }

    fun getPendingFriendRequestsWithUserInfo(){
        viewModelScope.launch {
            _friendRequestAndUserInfo.value = friendRequestRepository.getPendingFriendRequestsWithUserInfo()
        }
    }

    fun respondToFriendRequest(requestId: String, accept: Boolean,onResult: (Boolean) -> Unit){
        viewModelScope.launch {
            friendRequestRepository.respondToFriendRequest(requestId, accept){
                onResult(it)
            }
        }
    }

}