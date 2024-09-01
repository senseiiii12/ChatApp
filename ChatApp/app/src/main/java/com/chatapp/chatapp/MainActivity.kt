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
import com.chatapp.chatapp.presentation.screens.LoginScreen.LoginScreen
import com.chatapp.chatapp.presentation.screens.MainEntrance.MainEntrance
import com.chatapp.chatapp.presentation.screens.RegisterScreen.BottomSheetRegister
import com.chatapp.chatapp.presentation.screens.RegisterScreen.SignUpViewModel
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                        }
                        composable(route = "chat/{otherUserJson}") { backStackEntry ->
                            val userJson = backStackEntry.arguments?.getString("otherUserJson") ?: ""
                            val chatViewModel: ChatViewModel = hiltViewModel()
                            val (chatId,otherUser) = chatViewModel.generateChatId(userJson)
                            ChatScreen(
                                chatId = chatId ,
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
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        currentUserId?.let { usersViewModel.updateUserStatus(it, true) }
    }

    override fun onStop() {
        super.onStop()
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        currentUserId?.let { usersViewModel.updateUserStatus(it, false) }
    }

}

