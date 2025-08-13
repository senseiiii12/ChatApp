package com.chatapp.chatapp.core.domain

import com.chatapp.chatapp.core.data.FriendRequestRepositoryImpl.RespondFriendResult
import com.chatapp.chatapp.core.domain.models.FriendRequest
import com.chatapp.chatapp.core.domain.models.FriendRequestWithUser

interface FriendRequestRepository {
    suspend fun sendFriendRequest(toUserId: String,onResult: (Boolean) -> Unit)
    suspend fun getPendingFriendRequestsWithUserInfo(): List<FriendRequestWithUser>
    suspend fun respondToFriendRequest(request: FriendRequest, accept: Boolean): RespondFriendResult
}