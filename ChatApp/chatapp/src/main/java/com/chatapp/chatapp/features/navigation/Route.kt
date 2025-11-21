package com.chatapp.chatapp.features.navigation


import kotlinx.serialization.Serializable

sealed interface RouteTypeSafe {
    sealed interface Graph : RouteTypeSafe {
        @Serializable
        data object AuthNavGraph : Graph
        @Serializable
        data object ChatRoomsNavGraph : Graph
    }

    sealed interface Screen : RouteTypeSafe {
        @Serializable
        object AuthScreen : Screen
        @Serializable
        object ChatRoomsScreen : Screen
        @Serializable
        object FriendsRequestsScreen : Screen
        @Serializable
        object MyFriendsScreen : Screen
        @Serializable
        object SearchUserScreen : Screen
        @Serializable
        data class ChatScreen(
            val otherUserJson: String,
            val currentUserJson: String
        ) : Screen
    }
}
