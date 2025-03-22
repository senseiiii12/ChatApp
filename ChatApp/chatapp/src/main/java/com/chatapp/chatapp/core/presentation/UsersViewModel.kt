package com.chatapp.chatapp.core.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.core.domain.UsersRepository
import com.chatapp.chatapp.features.auth.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val usersRepository: UsersRepository
) : ViewModel() {

    private val _currentUser = mutableStateOf(User())
    val currentUser = _currentUser

    private val _userStatuses = MutableStateFlow<Map<String, Pair<Boolean, Date>>>(emptyMap())
    val userStatuses = _userStatuses.asStateFlow()


    fun getCurrentUser(){
        viewModelScope.launch {
            usersRepository.getCurrentUser().collect { currentUser ->
                _currentUser.value = currentUser
            }
        }
    }

    fun listenForOtherUserStatus(userId: String) {
        usersRepository.listenForUserStatusChanges(userId) { (isOnline,lastSeen) ->
            _userStatuses.update { currentStatuses ->
                currentStatuses.toMutableMap().apply {
                    put(userId, Pair(isOnline,lastSeen))
                }
            }
        }
    }

    fun updateUserOnlineStatus(userId: String, isOnline: Boolean){
        usersRepository.updateUserOnlineStatus(userId, isOnline)
    }

}