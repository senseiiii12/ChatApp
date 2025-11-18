package com.chatapp.chatapp

import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.chatapp.chatapp.core.presentation.SplashViewModel
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.navigation.NavigationTransitions
import com.chatapp.chatapp.features.navigation.authFlow
import com.chatapp.chatapp.features.navigation.mainFlow
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
            val startDestination = splashViewModel.checkUserTypeSafe()

            ChatAppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PrimaryBackground),
                ) {
                    NavHost(
                        modifier = Modifier.background(PrimaryBackground),
                        navController = navController,
                        startDestination = startDestination,
                        enterTransition = { NavigationTransitions.slideHorizontal.enter },
                        exitTransition = { NavigationTransitions.slideHorizontal.exit },
                        popEnterTransition = { NavigationTransitions.slideHorizontal.popEnter },
                        popExitTransition = { NavigationTransitions.slideHorizontal.popExit }
                    ) {
                        authFlow(
                            navController = navController,
                            usersViewModel = usersViewModel
                        )

                        mainFlow(
                            navController = navController,
                            usersViewModel = usersViewModel
                        )
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

