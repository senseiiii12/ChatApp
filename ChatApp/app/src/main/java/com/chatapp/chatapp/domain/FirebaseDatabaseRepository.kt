package com.chatapp.chatapp.domain

import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.util.Resource
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface FirebaseDatabaseRepository {

    fun saveUserToDatabase(user: Map<String, Any?>)
    suspend fun getUsersList(): Flow<Resource<List<User>>>
    fun updateUserStatus(userId: String, isOnline: Boolean)
    fun listenForUserStatusChanges(userId: String, onStatusChanged: (Pair<Boolean,Date>) -> Unit)
}