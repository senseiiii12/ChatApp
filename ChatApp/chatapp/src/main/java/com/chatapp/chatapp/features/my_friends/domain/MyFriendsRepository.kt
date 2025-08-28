package com.chatapp.chatapp.features.my_friends.domain

import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.util.Resource
import kotlinx.coroutines.flow.Flow

interface MyFriendsRepository {

    suspend fun getMyFriends(): Flow<Resource<List<User>>>

    suspend fun deleteFriend(friendId: String)

}