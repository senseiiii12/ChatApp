package com.chatapp.chatapp.domain

import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.util.Resource
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface UsersRepository {

    fun saveUserToDatabase(user: Map<String, Any?>)
    suspend fun getUsersList(): Flow<Resource<List<User>>>
    suspend fun searchUsers(query: String): Flow<List<User>>
    fun updateUserStatus(userId: String, isOnline: Boolean,onSuccesUpdateStatus:() -> Unit)
    fun listenForUserStatusChanges(userId: String, onStatusChanged: (Pair<Boolean,Date>) -> Unit)

}