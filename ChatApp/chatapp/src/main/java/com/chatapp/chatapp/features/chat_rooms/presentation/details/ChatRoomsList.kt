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
import com.chatapp.chatapp.features.chat_rooms.presentation.new_state.ChatRoomsState
import com.chatapp.chatapp.features.chat_rooms.presentation.new_state.ChatRoomsViewModel
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.google.firebase.auth.FirebaseAuth


@Composable
fun ChatRoomsList(
    stateChatRooms: List<ChatRoomsState>,
    onUserClick: (User) -> Unit,
    usersViewModel: UsersViewModel = hiltViewModel()
) {

    val firebaseCurrentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val isOnline by usersViewModel.userStatuses.collectAsState()

    Column {
        LazyColumn(
            contentPadding = PaddingValues(start = 0.dp, top = 8.dp, end = 0.dp),
        ) {
            items(stateChatRooms, key = { chatRoom -> chatRoom.chatId }) { chatRoom ->
                ChatRoomItem(
                    modifier = Modifier.animateItem(),
                    currentUserId = firebaseCurrentUserId,
                    state = chatRoom,
                    isOnline = isOnline[chatRoom.otherUser.userId]?.first ?: false,
                    onClick = { onUserClick(chatRoom.otherUser) }
                )
                Divider(color = PrimaryBackground)
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
