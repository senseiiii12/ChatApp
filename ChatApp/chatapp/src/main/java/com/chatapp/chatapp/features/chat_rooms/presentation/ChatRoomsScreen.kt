package com.chatapp.chatapp.features.chat_rooms.presentation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.features.navigation.Route
import com.chatapp.chatapp.features.chat_rooms.presentation.details.TopBarChatsRoom
import com.chatapp.chatapp.features.chat_rooms.presentation.details.ChatRoomsList
import com.chatapp.chatapp.features.chat_rooms.presentation.details.UserListItemShimmerEffect
import com.chatapp.chatapp.features.chat_rooms.presentation.new_state.ChatRoomsViewModel
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.Surface_Card
import com.chatapp.chatapp.util.NetworkConnection.NetworkConnectionIndicator
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomsScreen(
    navController: NavController,
) {
    val usersViewModel: UsersViewModel = hiltViewModel()

    val chatRoomsViewModel: ChatRoomsViewModel = hiltViewModel()
    val stateChatRooms = chatRoomsViewModel.chatRoomsState.collectAsState()

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(Surface_Card)
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    val usersState = usersViewModel.users.collectAsState()
    val currentUser = usersState.value.isSuccess.find { it.userId == currentUserId }


    LaunchedEffect(Unit) {
        usersViewModel.getUsers()
    }
    LaunchedEffect(Unit) {
        chatRoomsViewModel.loadChatRooms(currentUserId)
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
            if (usersState.value.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                repeat(10) {
                    UserListItemShimmerEffect()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            ChatRoomsList(
                stateChatRooms = stateChatRooms.value,
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
                usersViewModel.updateUserStatus(currentUserId, false)
                FirebaseAuth.getInstance().signOut()
                navigateToMainEntrance(navController)
            }) {
                Text(text = "${currentUser?.name}")
            }
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


