package com.chatapp.chatapp.presentation.screens.HomePage

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.presentation.navigation.Route
import com.chatapp.chatapp.presentation.screens.Chat.ChatViewModel
import com.chatapp.chatapp.presentation.screens.HomePage.details.SearchTextField
import com.chatapp.chatapp.presentation.screens.HomePage.details.TopBarHome
import com.chatapp.chatapp.presentation.screens.HomePage.details.UserList
import com.chatapp.chatapp.presentation.screens.HomePage.details.UserListItemShimmerEffect
import com.chatapp.chatapp.ui.theme.PrimaryBackground
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
        Log.d("Recomposition", "getUsers")
    }

    LaunchedEffect(filteredUsers) {
        chatViewModel.updateChatIds(chatIds)
        Log.d("Recomposition", "filteredUsers")
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
        if (usersState.value.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            repeat(6) {
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
            usersViewModel.updateUserStatus(currentUserId, false) {
                FirebaseAuth.getInstance().signOut()
                navigateToMainEntrance(navController)
            }
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


