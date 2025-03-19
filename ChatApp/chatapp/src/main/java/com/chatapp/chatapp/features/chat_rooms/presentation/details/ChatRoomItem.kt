package com.chatapp.chatapp.features.chat_rooms.presentation.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chatapp.chatapp.R
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat.domain.MessageStatus
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat_rooms.presentation.new_state.ChatRoomsState
import com.chatapp.chatapp.ui.theme.Bg_Default_Avatar
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.Mark_Message
import com.chatapp.chatapp.ui.theme.Online
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.PrimaryPurple
import com.chatapp.chatapp.ui.theme.Surface_Card
import com.chatapp.chatapp.util.TimeManager
import java.util.Date


@Composable
fun ChatRoomItem(
    modifier: Modifier = Modifier,
    state: ChatRoomsState,
    currentUserId: String,
    isOnline: Boolean,
    onClick: () -> Unit
) {
    val otherUser = state.otherUser
    val lastMessage = state.lastMessage

    val timeManager = TimeManager()
    val lastMessageText = if (currentUserId == lastMessage?.userId) "You: ${lastMessage.text}" else "${lastMessage?.text ?: ""}"
    val lastMessageColor = if (lastMessage?.status?.name.equals("READ")) ChatText else Color.White

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(0.dp))
            .clip(RoundedCornerShape(0.dp))
            .background(Surface_Card)
            .clickable { onClick() }
            .height(60.dp)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box {
            otherUser.avatar?.let {
                AsyncImage(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(30.dp),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(otherUser.avatar)
                        .crossfade(true)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            } ?: Image(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Bg_Default_Avatar)
                    .size(30.dp),
                painter = painterResource(id = R.drawable.defaulf_user_avatar),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )

            if (isOnline) {
                Box(
                    modifier = Modifier
                        .border(1.dp, PrimaryBackground, CircleShape)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .size(8.dp)
                        .background(Online)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = otherUser.name,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    color = ChatText
                )
                Text(
                    text = timeManager.getTimeSinceLastMessage(lastMessage),
                    fontSize = 8.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                    color = ChatText
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 24.dp),
                    text = lastMessageText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                    color = lastMessageColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (state.unreadMessageCount > 0 && currentUserId != lastMessage?.userId) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .heightIn(min = 16.dp, max = 16.dp)
                            .widthIn(min = 16.dp, max = 40.dp)
                            .background(PrimaryPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${state.unreadMessageCount}",
                            fontSize = 9.sp,
                            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                            color = Color.White
                        )
                    }
                } else {
                    when (lastMessage?.status) {
                        MessageStatus.DELIVERED -> {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Mark_Message
                            )
                        }
                        MessageStatus.READ -> {
                            if (currentUserId == lastMessage.userId) {
                                Image(
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(id = R.drawable.double_check_icon),
                                    contentDescription = null,
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun UserListItemPreview() {
    val testMessage: Message? = Message(
        userId = "123",
        text = "Привет",
        timestamp = 1L,
        messageId = "2222",
        status = MessageStatus.READ
    )

//    ChatAppTheme {
//        ChatRoomItem(
//            currentUserId = "123",
//            user = User(
//                userId = "123",
//                name = "Alexander",
//                email = "Free Download Check 134 SVG vector file in monocolor and multicolor type for Sketch",
//                password = "123",
//                lastSeen = Date(0)
//            ),
//            onClick = {},
//            lastMessage = testMessage,
//            isOnline = true,
//            newMessageCount = 1
//        )
//    }
}