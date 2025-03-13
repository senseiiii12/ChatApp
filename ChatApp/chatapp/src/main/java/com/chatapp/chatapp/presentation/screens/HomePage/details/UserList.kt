package com.chatapp.chatapp.presentation.screens.HomePage.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.presentation.screens.Chat.ChatViewModel
import com.chatapp.chatapp.presentation.screens.HomePage.UserListState
import com.chatapp.chatapp.presentation.screens.HomePage.UsersViewModel
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.google.firebase.auth.FirebaseAuth


@Composable
fun UserList(
    filteredUsers: List<User>,
    onUserClick: (User) -> Unit,
    chatViewModel: ChatViewModel = hiltViewModel(),
    usersViewModel: UsersViewModel = hiltViewModel()
) {

    val firebaseCurrentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val latestMessages by chatViewModel.latestMessages.collectAsState()
    val messageCounts by chatViewModel.messageCounts.collectAsState()
    val isOnline by usersViewModel.userStatuses.collectAsState()


    Column {
        LazyColumn(
            contentPadding = PaddingValues(start = 0.dp, top = 8.dp, end = 0.dp),
        ) {
            items(filteredUsers, key = { user -> user.userId }) { user ->
                val chatId = generateChatId(firebaseCurrentUserId, user.userId)
                if (latestMessages[chatId]?.text != ""){
                    UserListItem(
                        modifier = Modifier.animateItem(),
                        currentUserId = firebaseCurrentUserId,
                        user = user,
                        lastMessage = latestMessages[chatId],
                        newMessageCount = messageCounts[chatId] ?: 0,
                        isOnline = isOnline[user.userId]?.first ?: false,
                        onClick = { onUserClick(user) }
                    )
                    Divider(color = PrimaryBackground)
                }
            }
        }
    }
}


fun generateChatId(currentUserId: String, otherUserId: String): String {
    return if (currentUserId < otherUserId) {
        "${currentUserId}-${otherUserId}"
    } else {
        "${otherUserId}-${currentUserId}"
    }
}
