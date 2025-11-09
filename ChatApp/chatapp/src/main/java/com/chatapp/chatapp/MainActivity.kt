package com.chatapp.chatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chatapp.chatapp.core.presentation.SplashViewModel
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.auth.presentation.MainEntrance
import com.chatapp.chatapp.features.chat.presentation.ChatScreen
import com.chatapp.chatapp.features.chat.presentation.ChatViewModel
import com.chatapp.chatapp.features.chat_rooms.presentation.ChatRoomsScreen
import com.chatapp.chatapp.features.friend_requests.presentation.RequestsInFriendScreen
import com.chatapp.chatapp.features.my_friends.presentation.MyFriendsScreen
import com.chatapp.chatapp.features.navigation.Route
import com.chatapp.chatapp.features.search_user.presentation.SearchUsersScreen
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val splashViewModel by viewModels<SplashViewModel>()
    val usersViewModel by viewModels<UsersViewModel>()

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                splashViewModel.isLoading.value
            }
        }

        setContent {
            val navController = rememberNavController()
            val startDestination = splashViewModel.checkUser()
            val currentUserId = usersViewModel.currentUserId.collectAsState()


            LaunchedEffect(currentUserId) {
                Log.d("currentUserIdLaunchedEffect", "LaunchedEffect - ${currentUserId.value}")
            }

            ChatAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().background(PrimaryBackground),
                ) {
                    NavHost(
                        modifier = Modifier.background(PrimaryBackground),
                        navController = navController,
                        startDestination = startDestination,
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = spring()
                            ) + fadeIn()
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = spring()
                            ) + fadeOut()
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = spring()
                            ) + fadeIn()
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = spring()
                            ) + fadeOut()
                        }
                    ) {
                        composable(route = Route.MainEntrance.route) {
                            Log.d("currentUserIdonCreate", "MainEntrance - ${currentUserId.value}")
                            MainEntrance(
                                navController = navController,
                                usersViewModel = usersViewModel
                            )
                        }
                        composable(route = Route.HomePage.route) {
                            Log.d("currentUserIdonCreate", "ChatRoomsScreen - ${currentUserId.value}")
                            ChatRoomsScreen(
                                navController = navController,
                                usersViewModel = usersViewModel
                            )
                        }
                        composable(route = Route.FriendsRequests.route) {
                            RequestsInFriendScreen(navController = navController)
                        }
                        composable(route = Route.MyFriends.route) {
                            MyFriendsScreen(navController = navController)
                        }
                        composable(route = Route.SearchUsers.route) {
                            SearchUsersScreen(
                                navController = navController,
                                usersViewModel = usersViewModel
                            )
                        }
                        composable(route = "chat/{otherUserJson}/{currentUserJson}") { backStackEntry ->
                            val otherUserJson =
                                backStackEntry.arguments?.getString("otherUserJson") ?: ""
                            val currentUserJson =
                                backStackEntry.arguments?.getString("currentUserJson") ?: ""
                            val chatViewModel: ChatViewModel = hiltViewModel()
                            val (chatId, otherUser, currentUser) = remember(
                                otherUserJson,
                                currentUserJson
                            ) {
                                chatViewModel.generateChatId(otherUserJson, currentUserJson)
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
            }
        }
    }

    override fun onStart() {
        super.onStart()
        usersViewModel.currentUserId.value?.let {
            usersViewModel.updateUserOnlineStatus(it, true)
        }
    }

    override fun onStop() {
        super.onStop()
        usersViewModel.currentUserId.value?.let {
            usersViewModel.updateUserOnlineStatus(it, false)
        }
    }

}

