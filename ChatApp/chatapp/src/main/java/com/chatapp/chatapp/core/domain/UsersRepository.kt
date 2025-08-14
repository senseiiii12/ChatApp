package com.chatapp.chatapp.core.domain

import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.search_user.presentation.details.UserWithFriendRequest
import com.chatapp.chatapp.util.Resource
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface UsersRepository {

    suspend fun getCurrentUser(): Flow<User>
    suspend fun getUsersList(): Flow<Resource<List<User>>>
    suspend fun searchUsers(query: String): Flow<List<UserWithFriendRequest>>
    fun updateUserOnlineStatus(userId: String, isOnline: Boolean)
    fun listenForUserStatusChanges(userId: String, onStatusChanged: (Pair<Boolean, Date>) -> Unit)

}