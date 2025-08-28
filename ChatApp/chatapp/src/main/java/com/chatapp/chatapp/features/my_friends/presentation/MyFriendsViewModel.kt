package com.chatapp.chatapp.features.my_friends.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.my_friends.domain.MyFriendsRepository
import com.chatapp.chatapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyFriendsViewModel @Inject constructor(
    private val myFriendsRepository: MyFriendsRepository
): ViewModel() {

    private val _myFriendsState = MutableStateFlow(MyFriendsState())
    val myFriendsState = _myFriendsState.asStateFlow()

    init {
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            myFriendsRepository.getMyFriends().collect { resource ->
                when(resource){
                    is Resource.Error -> _myFriendsState.update {
                        it.copy(isLoading = false, isError = "Error friends")
                    }
                    is Resource.Loading -> _myFriendsState.update {
                        it.copy(isLoading = true, isError = "")
                    }
                    is Resource.Success -> _myFriendsState.update {
                        it.copy(data = resource.data,isLoading = false, isError = "")
                    }
                }
            }
        }
    }

    fun deleteFriend(friendId: String) {
        viewModelScope.launch {
            try {
                myFriendsRepository.deleteFriend(friendId)
                _myFriendsState.update { state ->
                    val updatedList = state.data?.filter { it.userId != friendId }
                    state.copy(data = updatedList, isLoading = false, isError = "")
                }
            } catch (e: Exception) {
                _myFriendsState.update {
                    it.copy(isLoading = false, isError = e.message ?: "Ошибка удаления")
                }
            }
        }
    }
}