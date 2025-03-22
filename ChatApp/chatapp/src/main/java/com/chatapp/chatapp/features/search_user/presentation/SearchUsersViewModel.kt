package com.chatapp.chatapp.features.search_user.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.core.domain.UsersRepository
import com.chatapp.chatapp.features.auth.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
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