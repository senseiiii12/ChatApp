package com.chatapp.chatapp.features.chat_rooms.presentation

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.chat_rooms.presentation.details.ChatRoomsList
import com.chatapp.chatapp.features.chat_rooms.presentation.details.TopBarChatsRoom
import com.chatapp.chatapp.features.navigation.Route
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.SecondaryBackground
import com.chatapp.chatapp.util.NetworkConnection.NetworkConnectionIndicator
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomsScreen(
    navController: NavController,
    usersViewModel: UsersViewModel
) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(SecondaryBackground)
    val chatRoomsViewModel: ChatRoomsViewModel = hiltViewModel()

    val stateChatRooms = chatRoomsViewModel.chatRooms.collectAsState()
    val currentUser = usersViewModel.currentUser.value
    val currentUserId = usersViewModel.currentUserId.collectAsState()

    BackHandler(enabled = true) {
        (navController.context as? Activity)?.finish()
    }
    LaunchedEffect(Unit) {
        usersViewModel.getCurrentUser()
    }
    LaunchedEffect(Unit) {
        chatRoomsViewModel.loadAndListenToChats(currentUserId.value.toString())
        stateChatRooms.value.map {
            usersViewModel.listenForOtherUserStatus(it.otherUser.userId)
        }
    }

    Scaffold(
        topBar = {
            TopBarChatsRoom(
                onSearchButtonClick = { navController.navigate(Route.SearchUsers.route) },
                onMenuButtonClick = {}
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryBackground)
                .padding(paddingValues)
        ) {
            NetworkConnectionIndicator()
            ChatRoomsList(
                stateChatRooms = stateChatRooms.value,
                usersViewModel = usersViewModel,
                onUserClick = { user ->
                    val otherUserJson = Gson().toJson(user)
                    val currentUserJson = Gson().toJson(currentUser)
                    navController.navigate(
                        "chat/${Uri.encode(otherUserJson)}/${
                            Uri.encode(currentUserJson)
                        }"
                    )
                }
            )
            Button(onClick = {
                chatRoomsViewModel.clearChatRooms()
                usersViewModel.logout()
                navigateToMainEntrance(navController)
            }) {
                Text(text = currentUserId.value.toString())
            }
        }
    }
}


fun navigateToMainEntrance(navController: NavController) {
    navController.navigate(Route.MainEntrance.route) {
        popUpTo(navController.graph.id) {
            inclusive = true
        }
    }
}


