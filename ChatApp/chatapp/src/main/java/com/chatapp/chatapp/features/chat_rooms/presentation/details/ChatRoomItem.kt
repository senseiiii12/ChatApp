package com.chatapp.chatapp.features.chat_rooms.presentation.details


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chatapp.chatapp.R
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.chat.domain.MessageStatus
import com.chatapp.chatapp.features.chat_rooms.domain.models.ChatRooms
import com.chatapp.chatapp.ui.theme.Bg_Default_Avatar
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.Online
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.PrimaryPurple
import com.chatapp.chatapp.util.ImageLoader.ImageLoaderViewModel
import com.chatapp.chatapp.util.TimeManager
import java.util.Date


@Composable
fun ChatRoomItem(
    modifier: Modifier = Modifier,
    state: ChatRooms,
    currentUserId: String,
    isOnline: Boolean,
    onClickChatRoom: () -> Unit
) {
    val otherUser = state.otherUser
    val lastMessage = state.lastMessage
    val context = LocalContext.current

    val imageLoaderViewModel: ImageLoaderViewModel = hiltViewModel()
    val lastMessageText =
        if (currentUserId == lastMessage?.userId) "You: ${lastMessage.text}" else "${lastMessage?.text ?: ""}"
    val lastMessageColor =
        if (lastMessage?.status?.name.equals("READ")) Color.White.copy(alpha = 0.5f) else Color.White.copy(
            alpha = 0.8f
        )
    val avatarUrl by remember { mutableStateOf(otherUser.avatar) }


    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PrimaryBackground)
            .clickable { onClickChatRoom() }
            .height(76.dp)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box {
            otherUser.avatar?.let {
                AsyncImage(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(60.dp),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .diskCacheKey(avatarUrl)
                        .memoryCacheKey(avatarUrl)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    imageLoader = imageLoaderViewModel.imageLoader,
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            } ?: Image(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Bg_Default_Avatar)
                    .size(60.dp),
                painter = painterResource(id = R.drawable.defaulf_user_avatar),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )

            if (isOnline) {
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp, end = 3.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .size(10.dp)
                        .background(Online)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = otherUser.name,
                    style = MyCustomTypography.SemiBold_18,
                    color = Color.White
                )
                Text(
                    text = TimeManager.getTimeSinceLastMessage(lastMessage),
                    style = MyCustomTypography.Normal_10,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = lastMessageText,
                    transitionSpec = {
                        slideInVertically { height -> -height } + fadeIn() togetherWith
                                slideOutVertically { height -> height } + fadeOut()
                    },
                    label = "lastMessageAnimation"
                ) { targetMessage ->
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 24.dp),
                        text = targetMessage,
                        style = MyCustomTypography.Medium_14,
                        color = lastMessageColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (state.unreadMessageCount > 0 && currentUserId != lastMessage?.userId) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .heightIn(min = 16.dp, max = 16.dp)
                            .widthIn(min = 16.dp, max = 40.dp)
                            .background(PrimaryPurple)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${state.unreadMessageCount}",
                            style = MyCustomTypography.Bold_8,
                            color = Color.White
                        )
                    }
                } else {
                    when (lastMessage?.status) {
                        MessageStatus.DELIVERED -> {
                            Image(
                                modifier = Modifier.size(12.dp),
                                painter = painterResource(id = R.drawable.ic_message_delivered),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Online)
                            )
                        }

                        MessageStatus.READ -> {
                            if (currentUserId == lastMessage.userId) {
                                Image(
                                    modifier = Modifier.size(14.dp),
                                    painter = painterResource(id = R.drawable.ic_message_read),
                                    contentDescription = null,
                                    colorFilter = ColorFilter.tint(Online)
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
    val testMessage = Message(
        userId = "123",
        text = "Привет",
        timestamp = 1L,
        messageId = "2222",
        status = MessageStatus.READ
    )
    val testUser = User(
        userId = "123",
        name = "Александр Чепига",
        email = "Free Download Check 134 SVG vector file in monocolor and multicolor type for Sketch",
        password = "123",
        lastSeen = Date(0)
    )
    val testState = ChatRooms(
        chatId = "123",
        otherUser = testUser,
        isOnline = true,
        lastMessage = testMessage,
        unreadMessageCount = 1
    )

    ChatAppTheme {
        ChatRoomItem(
            currentUserId = "1235",
            state = testState,
            isOnline = true,
            onClickChatRoom = {}
        )
    }
}