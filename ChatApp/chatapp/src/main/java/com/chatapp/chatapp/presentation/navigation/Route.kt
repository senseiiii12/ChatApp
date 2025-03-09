package com.chatapp.chatapp.presentation.navigation

sealed class Route(val route: String) {
    object MainEntrance: Route(route = "mainEntrance")
    object HomePage: Route(route = "homePage")
    object SearchUsers: Route(route = "searchUsers")
    object Notification: Route(route = "notification")
}