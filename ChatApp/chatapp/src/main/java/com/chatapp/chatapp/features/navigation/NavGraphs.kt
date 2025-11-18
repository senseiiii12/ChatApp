package com.chatapp.chatapp.features.navigation

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.auth.presentation.MainEntrance
import com.chatapp.chatapp.features.chat.presentation.ChatScreen
import com.chatapp.chatapp.features.chat.presentation.ChatViewModel
import com.chatapp.chatapp.features.chat_rooms.presentation.ChatRoomsScreen
import com.chatapp.chatapp.features.friend_requests.presentation.RequestsInFriendScreen
import com.chatapp.chatapp.features.my_friends.presentation.MyFriendsScreen
import com.chatapp.chatapp.features.search_user.presentation.SearchUsersScreen

/**
 * Граф авторизации
 */
fun NavGraphBuilder.authFlow(
    navController: NavHostController,
    usersViewModel: UsersViewModel
) {
    animatedNavigation<AuthFlow, MainEntrance>(
        animation = NavigationAnimation.SlideHorizontal
    ) {
        animatedComposable<MainEntrance> {
            MainEntrance(
                navController = navController,
                usersViewModel = usersViewModel
            )
        }
    }
}

/**
 * Главный граф приложения (после авторизации)
 */
fun NavGraphBuilder.mainFlow(
    navController: NavHostController,
    usersViewModel: UsersViewModel
) {
    animatedNavigation<MainFlow, HomePage>(
        animation = NavigationAnimation.SlideHorizontal
    ) {
        animatedComposable<HomePage>(
            animation = NavigationAnimation.SlideHorizontal
        ) {
            ChatRoomsScreen(
                navController = navController,
                usersViewModel = usersViewModel
            )
        }
        animatedComposable<FriendsRequests>(
            animation = NavigationAnimation.SlideHorizontal
        ) {
            RequestsInFriendScreen(navController = navController)
        }
        animatedComposable<MyFriends>(
            animation = NavigationAnimation.SlideHorizontal
        ) {
            MyFriendsScreen(navController = navController)
        }
        animatedComposable<SearchUsers>(
            animation = NavigationAnimation.SlideHorizontal
        ) {
            SearchUsersScreen(
                navController = navController,
                usersViewModel = usersViewModel
            )
        }
        animatedComposable<Chat>(
            animation = NavigationAnimation.SlideHorizontal
        ) { backStackEntry ->
            val chatRoute = backStackEntry.toRoute<Chat>()
            val chatViewModel: ChatViewModel = hiltViewModel()

            val (chatId, otherUser, currentUser) = remember(
                chatRoute.otherUserJson,
                chatRoute.currentUserJson
            ) {
                chatViewModel.generateChatId(
                    chatRoute.otherUserJson,
                    chatRoute.currentUserJson
                )
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