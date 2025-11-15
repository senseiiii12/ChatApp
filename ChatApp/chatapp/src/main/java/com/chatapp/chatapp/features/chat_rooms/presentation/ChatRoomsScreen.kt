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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat_rooms.domain.models.ChatRooms
import com.chatapp.chatapp.features.chat_rooms.presentation.details.ChatRoomsList
import com.chatapp.chatapp.features.chat_rooms.presentation.details.TopBarChatsRoom
import com.chatapp.chatapp.features.navigation.Route
import com.chatapp.chatapp.ui.theme.Green100
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.PrimaryPurple
import com.chatapp.chatapp.ui.theme.SecondaryBackground
import com.chatapp.chatapp.util.NetworkConnection.NetworkConnectionIndicator
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.gson.Gson
import com.snackbar.snackswipe.SnackSwipeBox
import com.snackbar.snackswipe.SnackSwipeController
import com.snackbar.snackswipe.showSnackSwipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomsScreen(
    navController: NavController,
    usersViewModel: UsersViewModel,
    chatRoomsViewModel: ChatRoomsViewModel = hiltViewModel()
) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(SecondaryBackground)

    val chatRooms by chatRoomsViewModel.chatRooms.collectAsState()
    val isLoading by chatRoomsViewModel.isLoading.collectAsState()
    val error by chatRoomsViewModel.error.collectAsState()
    val currentUser by usersViewModel.currentUser.collectAsState()
    val currentUserId by usersViewModel.currentUserId.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(enabled = true) {
        (navController.context as? Activity)?.finish()
    }

    LaunchedEffect(currentUserId) {
        currentUserId?.let {
            usersViewModel.getCurrentUser()
        }
    }

    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            chatRoomsViewModel.loadAndListenToChats(userId)
        }
    }

//    LaunchedEffect(chatRooms) {
//        chatRooms.forEach { chatRoom ->
//            usersViewModel.listenForOtherUserStatus(chatRoom.otherUser.userId)
//        }
//    }

    LaunchedEffect(error) {
        error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                withDismissAction = true
            )
            chatRoomsViewModel.clearError()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            chatRoomsViewModel.stopListening()
        }
    }

    SnackSwipeBox { snackSwipeController ->
        Scaffold(
            containerColor = PrimaryBackground,
            topBar = {
                TopBarChatsRoom(
                    onSearchButtonClick = {
                        navController.navigate(Route.SearchUsers.route)
                    },
                    onMenuButtonClick = {  }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    containerColor = PrimaryPurple,
                    onClick = {
                        showCustomSnackbar(
                            snackbarController = snackSwipeController,
                            onNavigateToRequests = {
                                navController.navigate(Route.FriendsRequests.route)
                            }
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Создать чат",
                        tint = Color.White
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PrimaryBackground)
                    .padding(paddingValues)
            ) {
                NetworkConnectionIndicator()

                ChatRoomsContent(
                    chatRooms = chatRooms,
                    isLoading = isLoading,
                    currentUser = currentUser,
                    currentUserId = currentUserId,
                    usersViewModel = usersViewModel,
                    chatRoomsViewModel = chatRoomsViewModel,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun ChatRoomsContent(
    chatRooms: List<ChatRooms>,
    isLoading: Boolean,
    currentUser: User?,
    currentUserId: String?,
    usersViewModel: UsersViewModel,
    chatRoomsViewModel: ChatRoomsViewModel,
    navController: NavController
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading && chatRooms.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryPurple
                )
            }
            chatRooms.isEmpty() && !isLoading -> {
                EmptyChatsState(
                    modifier = Modifier.align(Alignment.Center),
                    onNavigateToSearch = {
                        navController.navigate(Route.SearchUsers.route)
                    }
                )
            }
            else -> {
                ChatRoomsList(
                    stateChatRooms = chatRooms,
                    usersViewModel = usersViewModel,
                    onUserClick = { user ->
                        currentUser?.let { current ->
                            navigateToChat(
                                navController = navController,
                                otherUser = user,
                                currentUser = current
                            )
                        }
                    }
                )
            }
        }

        // Debug кнопка
        if (currentUserId != null) {
            DebugLogoutButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                currentUserId = currentUserId,
                onLogout = {
                    chatRoomsViewModel.clearChatRooms()
                    usersViewModel.logout()
                    navigateToMainEntrance(navController)
                }
            )
        }
    }
}

@Composable
private fun EmptyChatsState(
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "У вас пока нет чатов",
            style = MyCustomTypography.Bold_14,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable { onNavigateToSearch() },
            text = "Найти пользователей",
            style = MyCustomTypography.SemiBold_14,
            color = PrimaryPurple,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DebugLogoutButton(
    modifier: Modifier = Modifier,
    currentUserId: String,
    onLogout: () -> Unit
) {
    androidx.compose.material3.Button(
        modifier = modifier,
        onClick = onLogout
    ) {
        Text(text = "Выйти ($currentUserId)")
    }
}

private fun showCustomSnackbar(
    snackbarController: SnackSwipeController,
    onNavigateToRequests: () -> Unit
) {
    snackbarController.showSnackSwipe(
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Green100
            )
        },
        messageText = {
            Text(
                text = "У вас есть новые запросы в друзья",
                style = MyCustomTypography.SemiBold_12,
                color = Color.White.copy(alpha = 0.8f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
            )
        },
        customAction = {
            Text(
                modifier = Modifier
                    .clickable {
                        onNavigateToRequests()
                        snackbarController.close()
                    }
                    .padding(8.dp),
                text = "Открыть",
                style = MyCustomTypography.Bold_14,
                color = PrimaryPurple
            )
        },
        dismissAction = {
            IconButton(
                onClick = { snackbarController.close() }
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
        innerPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        outerPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    )
}

private fun navigateToChat(
    navController: NavController,
    otherUser: User,
    currentUser: User
) {
    val otherUserJson = Gson().toJson(otherUser)
    val currentUserJson = Gson().toJson(currentUser)
    navController.navigate(
        "chat/${Uri.encode(otherUserJson)}/${Uri.encode(currentUserJson)}"
    )
}

private fun navigateToMainEntrance(navController: NavController) {
    navController.navigate(Route.MainEntrance.route) {
        popUpTo(navController.graph.id) {
            inclusive = true
        }
    }
}