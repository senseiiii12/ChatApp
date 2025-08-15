package com.chatapp.chatapp.features.search_user.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatapp.chatapp.core.domain.FriendRequestRepository
import com.chatapp.chatapp.core.domain.UsersRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchUsersViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val friendRequestRepository: FriendRequestRepository,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {

    private val currentUserId: String = firebaseAuth.currentUser?.uid
        ?: throw IllegalStateException("User not logged in")

    private val _searchResults = MutableStateFlow<List<UserWithFriendRequest>>(emptyList())
    val searchResults: StateFlow<List<UserWithFriendRequest>> = _searchResults

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun observeSearchQuery(query: String) {
        viewModelScope.launch {
            flowOf(query)
                .debounce(300)
                .flatMapLatest { query ->
                    combine(
                        usersRepository.searchUsers(query),
                        friendRequestRepository.getFriendRequestsForSearchUser()
                    ) { users, requests ->
                        users.map { user ->
                            val incoming = requests.find { it.fromUserId == user.userId && it.toUserId == currentUserId }
                            val outgoing = requests.find { it.toUserId == user.userId && it.fromUserId == currentUserId }
                            UserWithFriendRequest(
                                user = user,
                                incomingRequest = incoming,
                                outgoingRequest = outgoing
                            )
                        }
                    }
                }
                .collect { combinedList ->
                    _searchResults.value = combinedList
                }
        }
    }
}