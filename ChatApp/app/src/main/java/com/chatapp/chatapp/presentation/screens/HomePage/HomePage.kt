package com.chatapp.chatapp.presentation.screens.HomePage

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.unpackFloat1
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.presentation.navigation.Route
import com.chatapp.chatapp.presentation.screens.Chat.ChatViewModel
import com.chatapp.chatapp.presentation.screens.HomePage.details.UserListItemShimmerEffect
import com.chatapp.chatapp.presentation.screens.HomePage.details.UserListScreen
import com.chatapp.chatapp.presentation.screens.HomePage.details.shimmerEffect
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.Outline_Card
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.Surface_Card
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson


@Composable
fun HomePage(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(PrimaryBackground)

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val usersViewModel: UsersViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val usersState = usersViewModel.users.collectAsState()
    val filteredUsers = usersState.value.isSuccess.filter { it.userId != currentUserId }


    LaunchedEffect(Unit) {
        usersViewModel.getUsers()
    }

    LaunchedEffect(filteredUsers) {
        val chatIds = filteredUsers.map { user ->
            val otherUserId = user.userId
            if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"
        }
        chatViewModel.updateChatIds(chatIds)
        chatViewModel.startListeningToChats()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {

        if (usersState.value.isLoading){
            Spacer(modifier = Modifier.height(16.dp))
            repeat(6){
                UserListItemShimmerEffect(
                    state = usersState.value
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        UserListScreen(
            stateUserList = usersState.value,
            filteredUsers = filteredUsers,
            onUserClick = { user ->
                val otherUserJson = Gson().toJson(user)
                navController.navigate("chat/${otherUserJson}")
            }
        )
        Button(onClick = {
            usersViewModel.updateUserStatus(currentUserId,false)
            FirebaseAuth.getInstance().signOut()
            navController.navigate(Route.MainEntrance.route)
        }) {
            Text(text = "${usersViewModel.getUserById(currentUserId)?.name}")
        }
    }
}



