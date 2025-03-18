package com.chatapp.chatapp.features.search_user.domain

interface FriendRequestRepository {
    suspend fun sendFriendRequest(toUserId: String,onResult: (Boolean) -> Unit)
    suspend fun getPendingFriendRequestsWithUserInfo(): List<FriendRequestWithUser>
    suspend fun respondToFriendRequest(requestId: String, accept: Boolean,onResult: (Boolean) -> Unit)

}