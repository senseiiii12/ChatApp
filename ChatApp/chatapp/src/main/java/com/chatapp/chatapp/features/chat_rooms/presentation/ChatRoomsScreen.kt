package com.chatapp.chatapp.features.chat_rooms.presentation

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.chat_rooms.presentation.details.ChatRoomsList
import com.chatapp.chatapp.features.chat_rooms.presentation.details.TopBarChatsRoom
import com.chatapp.chatapp.features.navigation.Route
import com.chatapp.chatapp.ui.theme.Green100
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.PrimaryPurple
import com.chatapp.chatapp.ui.theme.SecondaryBackground
import com.chatapp.chatapp.util.CustomSnackbar.CustomSnackbarHost
import com.chatapp.chatapp.util.CustomSnackbar.SnackbarController
import com.chatapp.chatapp.util.CustomSnackbar.SnackbarData
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

    val snackbarController = remember { SnackbarController() }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopBarChatsRoom(
                    onSearchButtonClick = { navController.navigate(Route.SearchUsers.route) },
                    onMenuButtonClick = {}
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    containerColor = PrimaryPurple,
                    onClick = {
                        MySnackBar(
                            snackbarController = snackbarController,
                            onCustomAction = {
                                navController.navigate(Route.FriendsRequests.route)
                            }
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
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
        CustomSnackbarHost(
            controller = snackbarController,
            modifier = Modifier
        )


    }
}


fun MySnackBar(
    snackbarController: SnackbarController,
    onCustomAction: () -> Unit
) {
    snackbarController.show(
        SnackbarData(
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Green100
                )
            },
            text = {
                Text(
                    text = "Привет, это кастомный Snackbar3333333333",
                    style = MyCustomTypography.SemiBold_12,
                    color = Color.White.copy(alpha = 0.5f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            },
            customAction = {
                Text(
                    modifier = Modifier.clickable { onCustomAction() },
                    text = "Send",
                    style = MyCustomTypography.Bold_14,
                    color = Color.White
                )
            },
            dismissAction = {
                IconButton(
                    onClick = {
                        snackbarController.close()
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White
                    )
                }
            },
            backgroundColor = Color.Black,
            durationMillis = 5000,
            innerPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            outerPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        )
    )
}

fun navigateToMainEntrance(navController: NavController) {
    navController.navigate(Route.MainEntrance.route) {
        popUpTo(navController.graph.id) {
            inclusive = true
        }
    }
}


