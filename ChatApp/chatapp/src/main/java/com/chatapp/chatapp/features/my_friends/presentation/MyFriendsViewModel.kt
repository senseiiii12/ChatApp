package com.chatapp.chatapp.features.my_friends.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.my_friends.domain.MyFriendsRepository
import com.chatapp.chatapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyFriendsViewModel @Inject constructor(
    private val myFriendsRepository: MyFriendsRepository
): ViewModel() {

    private val _myFriendsState = MutableStateFlow<Resource<List<User>>>(Resource.Loading())
    val myFriendsState = _myFriendsState.asStateFlow()

    init {
        loadFriends()
    }

    fun loadFriends() {
        viewModelScope.launch {
            myFriendsRepository.getMyFriends().collect { resource ->
                _myFriendsState.value = resource
            }
        }
    }
}