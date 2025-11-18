package com.chatapp.chatapp.features.navigation


import kotlinx.serialization.Serializable

// Главные Flow (графы навигации)
@Serializable object AuthFlow
@Serializable object MainFlow

// Auth Flow Routes
@Serializable object MainEntrance

// Main Flow Routes
@Serializable object HomePage
@Serializable object FriendsRequests
@Serializable object MyFriends
@Serializable object SearchUsers

// Chat Route с параметрами
@Serializable data class Chat(
    val otherUserJson: String,
    val currentUserJson: String
)

sealed class Route(val route: String) {
    object MainEntrance: Route(route = "mainEntrance")
    object HomePage: Route(route = "homePage")
    object SearchUsers: Route(route = "searchUsers")
    object FriendsRequests: Route(route = "friendsRequests")
    object MyFriends: Route(route = "myFriends")
}