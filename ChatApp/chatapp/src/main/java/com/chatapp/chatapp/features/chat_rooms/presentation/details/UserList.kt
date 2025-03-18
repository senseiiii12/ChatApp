package com.chatapp.chatapp.features.chat_rooms.presentation.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.presentation.ChatViewModel
import com.chatapp.chatapp.features.chat_rooms.presentation.UsersViewModel
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
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp),
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
