package com.chatapp.chatapp.features.chat_rooms.presentation.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat_rooms.presentation.ChatRoomsState
import com.chatapp.chatapp.ui.theme.SecondaryBackground


@Composable
fun ChatRoomsList(
    stateChatRooms: List<ChatRoomsState>,
    onUserClick: (User) -> Unit,
    usersViewModel: UsersViewModel,
) {

    val currentUserId = usersViewModel.currentUser.value.userId
    val isOnline by usersViewModel.userStatuses.collectAsState()

    Column {
        LazyColumn {
            items(stateChatRooms, key = { chatRoom -> chatRoom.chatId }) { chatRoom ->
                ChatRoomItem(
                    modifier = Modifier.animateItem(),
                    currentUserId = currentUserId,
                    state = chatRoom,
                    isOnline = isOnline[chatRoom.otherUser.userId]?.first ?: false,
                    onClick = { onUserClick(chatRoom.otherUser) }
                )
                Divider(
                    modifier = Modifier.padding(start = 88.dp),
                    color = SecondaryBackground
                )
            }
        }
    }
}



