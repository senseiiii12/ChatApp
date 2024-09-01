package com.chatapp.chatapp.presentation.screens.HomePage

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.domain.FirebaseDatabaseRepository
import com.chatapp.chatapp.domain.MessageRepository
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val firebaseDatabaseRepository: FirebaseDatabaseRepository
) : ViewModel() {

    private val _users = MutableStateFlow(UserListState(isLoading = false, isSuccess = emptyList(), isError = null))
    val users = _users.asStateFlow()

    private val _userStatuses = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val userStatuses = _userStatuses.asStateFlow()


    fun getUsers() {
        if (_users.value.isSuccess.isEmpty()) {
            viewModelScope.launch {
                firebaseDatabaseRepository.getUsersList().collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _users.value = UserListState(isLoading = true, isSuccess = emptyList(), isError = null)
                        }
                        is Resource.Success -> {
                            _users.value = UserListState(isLoading = false, isSuccess = result.data ?: emptyList(), isError = null)
                            result.data?.forEach { user ->
                                listenForUserStatus(user.userId)
                            }
                        }
                        is Resource.Error -> {
                            _users.value = UserListState(isLoading = false, isSuccess = emptyList(), isError = result.message)
                        }
                    }
                }
            }
        }
    }

//    private fun listenForUserStatus(userId: String) {
//        firebaseDatabaseRepository.listenForUserStatusChanges(userId) { isOnline ->
//            _userStatuses.update { currentStatuses ->
//                currentStatuses.toMutableMap().apply {
//                    put(userId, isOnline)
//                }
//            }
//        }
//    }
    private fun listenForUserStatus(userId: String) {
        firebaseDatabaseRepository.listenForUserStatusChanges(userId) { isOnline ->
            _userStatuses.update { currentStatuses ->
                val updatedStatuses = currentStatuses.toMutableMap().apply {
                    put(userId, isOnline)
                }
                // Обновляем список пользователей с новым статусом
                val updatedUsers = _users.value.isSuccess.map { user ->
                    if (user.userId == userId) {
                        user.copy(online = isOnline)
                    } else {
                        user
                    }
                }
                _users.value = _users.value.copy(isSuccess = updatedUsers)
                updatedStatuses
            }
        }
    }

    fun updateUserStatus(userId: String, isOnline: Boolean){
        firebaseDatabaseRepository.updateUserStatus(userId, isOnline)
    }

    // Функция для получения пользователя по userId
    fun getUserById(userId: String): User? {
        return _users.value.isSuccess.find { it.userId == userId }
    }


}