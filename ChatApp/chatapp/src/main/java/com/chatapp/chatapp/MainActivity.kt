package com.chatapp.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chatapp.chatapp.features.navigation.Route
import com.chatapp.chatapp.features.chat.presentation.ChatScreen
import com.chatapp.chatapp.features.chat.presentation.ChatViewModel
import com.chatapp.chatapp.features.chat_rooms.presentation.ChatRoomsScreen
import com.chatapp.chatapp.features.chat_rooms.presentation.UsersViewModel
import com.chatapp.chatapp.features.auth.presentation.MainEntrance
import com.chatapp.chatapp.features.friend_requests.presentation.NotificationScreen
import com.chatapp.chatapp.features.search_user.presentation.SearchUsersScreen
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val splashViewModel by viewModels<SplashViewModel>()
    val usersViewModel by viewModels<UsersViewModel>()

    var currentUserId: String? = null

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                splashViewModel.isLoading.value
            }
        }

        setContent {
            window.statusBarColor = getColor(R.color.PrimaryBackground)
            val navController = rememberNavController()
            val startDestination = splashViewModel.checkUser()

            ChatAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        enterTransition = {
                            fadeIn(
                                initialAlpha = 1f,
                                animationSpec = tween(500, easing = FastOutSlowInEasing))
                        },
                        exitTransition = {
                            fadeOut(
                                targetAlpha = 1f,
                                animationSpec = tween(500, easing = FastOutSlowInEasing))
                        }
                    ) {
                        composable(route = Route.MainEntrance.route) {
                            MainEntrance(navController = navController)
                        }
                        composable(route = Route.HomePage.route) {
                            ChatRoomsScreen(navController = navController)
                        }
                        composable(route = Route.Notification.route) {
                            NotificationScreen(navController = navController)
                        }
                        composable(route = Route.SearchUsers.route) {
                            SearchUsersScreen(navController = navController)
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
                                chatViewModel = chatViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        currentUserId?.let { usersViewModel.updateUserOnlineStatus(it, true) }
    }

    override fun onStop() {
        super.onStop()
        currentUserId?.let { usersViewModel.updateUserOnlineStatus(it, false) }
    }

}

