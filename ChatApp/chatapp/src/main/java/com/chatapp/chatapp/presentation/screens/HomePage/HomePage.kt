package com.chatapp.chatapp.presentation.screens.HomePage

import android.net.Uri
import android.util.Log
import android.widget.ProgressBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.R
import com.chatapp.chatapp.presentation.navigation.Route
import com.chatapp.chatapp.presentation.screens.Chat.ChatViewModel
import com.chatapp.chatapp.presentation.screens.HomePage.details.SearchTextField
import com.chatapp.chatapp.presentation.screens.HomePage.details.TopBarHome
import com.chatapp.chatapp.presentation.screens.HomePage.details.UserList
import com.chatapp.chatapp.presentation.screens.HomePage.details.UserListItemShimmerEffect
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.util.NetworkConnection.NetworkConnectionIndicator
import com.chatapp.chatapp.util.NetworkConnection.NetworkConnectionState
import com.chatapp.chatapp.util.NetworkConnection.rememberConnectivityState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson


@Composable
fun HomePage(
    navController: NavController,
) {
    val usersViewModel: UsersViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(PrimaryBackground)
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    val usersState = usersViewModel.users.collectAsState()
    val filteredUsers =  usersState.value.isSuccess.filter { it.userId != currentUserId }
    val currentUser =  usersState.value.isSuccess.find { it.userId == currentUserId }

    val chatIds = remember(filteredUsers) {
        filteredUsers.map { user ->
            val otherUserId = user.userId
            if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"
        }
    }

    LaunchedEffect(Unit) {
        usersViewModel.getUsers()
        chatViewModel.startListeningToChats()
    }

    LaunchedEffect(filteredUsers) {
        chatViewModel.updateChatIds(chatIds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        TopBarHome(
            onNotificationClick = {
                navController.navigate(Route.Notification.route)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        SearchTextField(
            enabled = false,
            value = "",
            onValueChange = {},
            onSeachFieldClick = {
                navController.navigate(Route.SearchUsers.route)
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        NetworkConnectionIndicator()

        if (usersState.value.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            repeat(10) {
                UserListItemShimmerEffect()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        UserList(
            filteredUsers = filteredUsers,
            onUserClick = { user ->
                val otherUserJson = Gson().toJson(user)
                val currentUserJson = Gson().toJson(currentUser)
                navController.navigate("chat/${Uri.encode(otherUserJson)}/${
                    Uri.encode(currentUserJson)}"
                )
            }
        )
        Button(onClick = {
            usersViewModel.updateUserStatus(currentUserId, false)
            FirebaseAuth.getInstance().signOut()
            navigateToMainEntrance(navController)
        }) {
            Text(text = "${currentUser?.name}")
        }
    }


}


fun navigateToMainEntrance(navController: NavController) {
    navController.navigate(Route.MainEntrance.route) {
        popUpTo(navController.graph.startDestinationId) {
            inclusive = true
        }
        launchSingleTop = true
    }
}


