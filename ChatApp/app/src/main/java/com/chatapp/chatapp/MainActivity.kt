package com.chatapp.chatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.presentation.navigation.Route
import com.chatapp.chatapp.presentation.screens.Chat.ChatScreen
import com.chatapp.chatapp.presentation.screens.Chat.ChatViewModel
import com.chatapp.chatapp.presentation.screens.HomePage.HomePage
import com.chatapp.chatapp.presentation.screens.HomePage.UsersViewModel
import com.chatapp.chatapp.presentation.screens.MainEntrance.LoginScreen.LoginScreen
import com.chatapp.chatapp.presentation.screens.MainEntrance.MainEntrance
import com.chatapp.chatapp.presentation.screens.MainEntrance.RegisterScreen.BottomSheetRegister
import com.chatapp.chatapp.presentation.screens.MainEntrance.RegisterScreen.SignUpViewModel
import com.chatapp.chatapp.presentation.screens.Notification.NotificationScreen
import com.chatapp.chatapp.presentation.screens.SearchUsers.SearchUsersScreen
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    val splashViewModel by viewModels<SplashViewModel>()
    val usersViewModel by viewModels<UsersViewModel>()

    var currentUserId: String? = null
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
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable(route = Route.MainEntrance.route) {
                            MainEntrance(navController = navController)
                        }
                        composable(route = Route.HomePage.route) {
                            HomePage(navController = navController)
                            Log.d("Recomposition", "HomePage enter")
                        }
                        composable(route = Route.Notification.route) {
                            NotificationScreen(navController = navController)
                            Log.d("Recomposition", "NotificationScreen enter")
                        }
                        composable(route = Route.SearchUsers.route) {
                            SearchUsersScreen(navController = navController)
                        }
                        composable(route = "chat/{otherUserJson}/{currentUserJson}") { backStackEntry ->
                            val otherUserJson = backStackEntry.arguments?.getString("otherUserJson") ?: ""
                            val currentUserJson = backStackEntry.arguments?.getString("currentUserJson") ?: ""
                            val chatViewModel: ChatViewModel = hiltViewModel()
                            val (chatId,otherUser,currentUser) = remember(otherUserJson, currentUserJson) {
                                chatViewModel.generateChatId(otherUserJson,currentUserJson)
                            }
                            Log.d("Recomposition", "ChatScreen enter")
                            ChatScreen(
                                chatId = chatId ,
                                currentUser = currentUser,
                                otherUser = otherUser,
                                navController = navController,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        currentUserId?.let { usersViewModel.updateUserStatus(it, true){} }
    }

    override fun onStop() {
        super.onStop()
        currentUserId?.let { usersViewModel.updateUserStatus(it, false){} }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentUserId?.let { usersViewModel.updateUserStatus(it, false){} }
    }

    override fun onPause() {
        super.onPause()
        currentUserId?.let { usersViewModel.updateUserStatus(it, false){} }
    }
}

