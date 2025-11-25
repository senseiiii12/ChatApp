package com.chatapp.chatapp.features.navigation

import androidx.navigation.NavController
import com.chatapp.chatapp.features.navigation.Route.Graph.AuthNavGraph
import com.chatapp.chatapp.features.navigation.Route.Graph.ChatRoomsNavGraph

fun NavController.navigateToChatRoomsNavGraph() {
    navigate(ChatRoomsNavGraph) {
        popUpTo(AuthNavGraph) { inclusive = true }
    }
}

fun NavController.navigateToAuthNavGraph() {
    navigate(AuthNavGraph) {
        popUpTo(0) { inclusive = true }
    }
}