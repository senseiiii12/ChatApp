package com.chatapp.chatapp.presentation.screens.SearchUsers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.domain.UsersRepository
import com.chatapp.chatapp.domain.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchUsersViewModel @Inject constructor(
    private val usersRepository: UsersRepository
): ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    fun searchUsers(query: String) {
        viewModelScope.launch {
            usersRepository.searchUsers(query).collect { userList ->
                _users.value = userList
            }
        }
    }
}