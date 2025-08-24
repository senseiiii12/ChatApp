package com.chatapp.chatapp.features.navigation

sealed class Route(val route: String) {
    object MainEntrance: Route(route = "mainEntrance")
    object HomePage: Route(route = "homePage")
    object SearchUsers: Route(route = "searchUsers")
    object FriendsRequests: Route(route = "friendsRequests")
    object MyFriends: Route(route = "myFriends")
}