package com.chatapp.chatapp.core.presentation

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.core.domain.UsersRepository
import com.chatapp.chatapp.features.auth.domain.User
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _currentUser = mutableStateOf(User())
    val currentUser = _currentUser

    private val _userStatuses = MutableStateFlow<Map<String, Pair<Boolean, Date>>>(emptyMap())
    val userStatuses = _userStatuses.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()

    init {
//        _currentUserId.value = auth.currentUser?.uid

        auth.addAuthStateListener { firebaseAuth ->
            _currentUserId.value = firebaseAuth.currentUser?.uid
            Log.d("addAuthStateListener",_currentUserId.value.toString())
        }
    }

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

    fun logout() {
        viewModelScope.launch {
            _currentUserId.value?.let { userId ->
                updateUserOnlineStatus(userId, false)
            }
            auth.signOut()
            clearViewModel()
        }
    }

    fun clearViewModel(){
        _currentUser.value = User()
        _userStatuses.value = emptyMap()
        _currentUserId.value = null
    }

}