package com.chatapp.chatapp.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.core.data.FriendRequestRepositoryImpl.RespondFriendResult
import com.chatapp.chatapp.core.domain.FriendRequestRepository
import com.chatapp.chatapp.core.domain.models.FriendRequest
import com.chatapp.chatapp.features.friend_requests.presentation.RequestsInFriendItemState
import com.chatapp.chatapp.features.friend_requests.presentation.RequestsInFriendScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val friendRequestRepository: FriendRequestRepository
): ViewModel() {

    private val _friendRequestsState = MutableStateFlow(RequestsInFriendScreenState())
    val friendRequestsState = _friendRequestsState.asStateFlow()

    fun sendFriendRequest(toUserId: String,onResult: (Boolean) -> Unit){
        viewModelScope.launch {
            friendRequestRepository.sendFriendRequest(toUserId){
                onResult(it)
            }
        }
    }

    fun getPendingFriendRequestsWithUserInfo() {
        viewModelScope.launch {
            _friendRequestsState.update { it.copy(isLoading = true, isError = "") }
            try {
                val requestsFromRepo = friendRequestRepository.getPendingFriendRequestsWithUserInfo()

                val items = requestsFromRepo.map { requestWithUser ->
                    RequestsInFriendItemState(
                        request = requestWithUser.friendRequest,
                        user = requestWithUser.user
                    )
                }

                _friendRequestsState.update {
                    it.copy(
                        requestsInFriendItemData = items,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _friendRequestsState.update {
                    it.copy(isLoading = false, isError = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun respondToFriendRequest(request: FriendRequest, accept: Boolean) {
        viewModelScope.launch {
            // Ставим loading для конкретного item
            _friendRequestsState.update { state ->
                state.copy(
                    requestsInFriendItemData = state.requestsInFriendItemData.map { item ->
                        if (item.request.id == request.id) {
                            if (accept) item.copy(isLoadingAccept = true) else item.copy(isLoadingDecline = true)
                        } else item
                    }
                )
            }

            val result = friendRequestRepository.respondToFriendRequest(request, accept)

            _friendRequestsState.update { state ->
                when (result) {
                    RespondFriendResult.SuccessAccept,
                    RespondFriendResult.SuccessDecline -> {
                        state.copy(
                            requestsInFriendItemData = state.requestsInFriendItemData.filterNot { it.request.id == request.id }
                        )
                    }
                    RespondFriendResult.ErrorAccept -> {
                        state.copy(
                            requestsInFriendItemData = state.requestsInFriendItemData.map { item ->
                                if (item.request.id == request.id) {
                                    item.copy(
                                        isLoadingAccept = false,
                                        isErrorAccept = "Не удалось добавить друга"
                                    )
                                } else item
                            }
                        )
                    }
                    RespondFriendResult.ErrorDecline -> {
                        state.copy(
                            requestsInFriendItemData = state.requestsInFriendItemData.map { item ->
                                if (item.request.id == request.id) {
                                    item.copy(
                                        isLoadingDecline = false,
                                        isErrorDecline = "Не удалось отклонить запрос"
                                    )
                                } else item
                            }
                        )
                    }
                }
            }
        }
    }


}