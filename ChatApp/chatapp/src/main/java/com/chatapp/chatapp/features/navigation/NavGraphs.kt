package com.chatapp.chatapp.features.navigation

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.auth.presentation.AuthScreen
import com.chatapp.chatapp.features.chat.presentation.ChatScreen
import com.chatapp.chatapp.features.chat.presentation.ChatViewModel
import com.chatapp.chatapp.features.chat_rooms.presentation.ChatRoomsScreen
import com.chatapp.chatapp.features.friend_requests.presentation.RequestsInFriendScreen
import com.chatapp.chatapp.features.my_friends.presentation.MyFriendsScreen
import com.chatapp.chatapp.features.search_user.presentation.SearchUsersScreen
import com.google.gson.Gson

/**
 * Граф авторизации
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController,
    usersViewModel: UsersViewModel
) {
    navigation<Route.Graph.AuthNavGraph>(startDestination = Route.Screen.AuthScreen) {
        composable<Route.Screen.AuthScreen> {
            AuthScreen(
                navController = navController,
                usersViewModel = usersViewModel
            )
        }
    }
}

/**
 * Главный граф приложения (после авторизации)
 */
fun NavGraphBuilder.chatRoomsNavGraph(
    navController: NavHostController,
    usersViewModel: UsersViewModel
) {
    navigation<Route.Graph.ChatRoomsNavGraph>(
        startDestination = Route.Screen.ChatRoomsScreen
    ) {
        composable<Route.Screen.ChatRoomsScreen>{
            ChatRoomsScreen(
                navController = navController,
                usersViewModel = usersViewModel
            )
        }
        composable<Route.Screen.FriendsRequestsScreen> {
            RequestsInFriendScreen(navController = navController)
        }
        composable<Route.Screen.MyFriendsScreen> {
            MyFriendsScreen(navController = navController)
        }
        composable<Route.Screen.SearchUserScreen> {
            SearchUsersScreen(
                navController = navController,
                usersViewModel = usersViewModel
            )
        }
        composable<Route.Screen.ChatScreen> { backStackEntry ->
            val gson = Gson()
            val chatRoute = backStackEntry.toRoute<Route.Screen.ChatScreen>()
            val chatViewModel: ChatViewModel = hiltViewModel()

            val otherUser = gson.fromJson(chatRoute.otherUserJson, User::class.java)
            val currentUser = gson.fromJson(chatRoute.currentUserJson, User::class.java)

            val chatId = remember(otherUser, currentUser) {
                chatViewModel.generateChatId(otherUser.userId, currentUser.userId)
            }

            ChatScreen(
                chatId = chatId,
                currentUser = currentUser,
                otherUser = otherUser,
                navController = navController,
                chatViewModel = chatViewModel,
                usersViewModel = usersViewModel
            )
        }
    }
}