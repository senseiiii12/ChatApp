package com.chatapp.chatapp.features.chat_rooms.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.features.chat_rooms.domain.UsersRepository
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.util.Resource
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

    private val _users = MutableStateFlow(UserListState())
    val users = _users.asStateFlow()

    private val _userStatuses = MutableStateFlow<Map<String, Pair<Boolean,Date>>>(emptyMap())
    val userStatuses = _userStatuses.asStateFlow()

    private var usersLoaded = false

    init {
        Log.d("ViewModel", "init UsersViewModel")
    }

    fun getUsers() {
        if (usersLoaded) return
        viewModelScope.launch {
            usersRepository.getUsersList().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _users.value = UserListState(isLoading = true)
                    }
                    is Resource.Success -> {
                        _users.value = UserListState(isLoading = false, isSuccess = result.data ?: emptyList())
                        usersLoaded = true
                        result.data?.forEach { user ->
                            listenForOtherUserStatus(user.userId)
                        }
                    }
                    is Resource.Error -> {
                        _users.value = UserListState(isLoading = false,isError = result.message)
                    }
                }
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


    fun updateUserStatus(userId: String, isOnline: Boolean){
        usersRepository.scheduleUpdateUserStatusWork(userId, isOnline)
    }



    // Функция для получения пользователя по userId
    fun getUserById(userId: String): User? {
        return _users.value.isSuccess.find { it.userId == userId }
    }


}