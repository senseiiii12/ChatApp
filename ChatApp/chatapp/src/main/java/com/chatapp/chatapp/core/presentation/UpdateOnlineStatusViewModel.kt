package com.chatapp.chatapp.core.presentation

import androidx.lifecycle.ViewModel
import com.chatapp.chatapp.core.domain.UsersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpdateOnlineStatusViewModel @Inject constructor(
    private val usersRepository: UsersRepository
): ViewModel() {

    fun updateUserOnlineStatus(userId: String, isOnline: Boolean){
        usersRepository.updateUserOnlineStatus(userId, isOnline)
    }

}