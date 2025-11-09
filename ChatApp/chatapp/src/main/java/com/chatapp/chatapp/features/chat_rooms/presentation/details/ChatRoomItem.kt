package com.chatapp.chatapp.features.chat_rooms.presentation.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chatapp.chatapp.R
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


@Composable
fun ChatRoomItem(
    modifier: Modifier = Modifier,
    chatRoomState: ChatRooms,
    currentUserId: String,
    onClickChatRoom: () -> Unit
) {
    val imageLoaderViewModel: ImageLoaderViewModel = hiltViewModel()
    val lastMessage = chatRoomState.lastMessage

    val isMyMessage = remember(lastMessage.userId, currentUserId) {
        lastMessage.userId == currentUserId
    }

    val annotatedLastMessageText = remember(lastMessage.text, isMyMessage) {
        buildAnnotatedString {
            if (isMyMessage) {
                withStyle(style = SpanStyle(color = PrimaryPurple)) {
                    append("You: ")
                }
            }
            append(lastMessage.text)
        }
    }

    val lastMessageColor = remember(lastMessage.status, isMyMessage) {
        when {
            isMyMessage && lastMessage.status == MessageStatus.READ ->
                Color.White.copy(alpha = 0.5f)
            else ->
                Color.White.copy(alpha = 0.8f)
        }
    }

    val timeText = remember(lastMessage.timestamp) {
        TimeManager.getTimeSinceLastMessage(lastMessage)
    }

    ChatRoomItemContent(
        modifier = modifier,
        userName = chatRoomState.otherUser.name,
        avatarUrl = chatRoomState.otherUser.avatar,
        isOnline = chatRoomState.otherUser.online,
        lastMessageText = annotatedLastMessageText,
        lastMessageColor = lastMessageColor,
        timeText = timeText,
        isMyMessage = isMyMessage,
        messageStatus = lastMessage.status,
        unreadCount = chatRoomState.unreadMessageCount,
        imageLoaderViewModel = imageLoaderViewModel,
        onClickChatRoom = onClickChatRoom
    )
}


@Composable
fun ChatRoomItemContent(
    modifier: Modifier = Modifier,
    userName: String,
    avatarUrl: String?,
    isOnline: Boolean,
    lastMessageText: AnnotatedString,
    lastMessageColor: Color,
    timeText: String,
    isMyMessage: Boolean,
    messageStatus: MessageStatus,
    unreadCount: Int,
    imageLoaderViewModel: ImageLoaderViewModel? = null,
    onClickChatRoom: () -> Unit
) {
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
        AvatarWithOnlineIndicator(
            avatarUrl = avatarUrl,
            isOnline = isOnline,
            imageLoaderViewModel = imageLoaderViewModel
        )

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
                    text = userName,
                    style = MyCustomTypography.SemiBold_18,
                    color = Color.White
                )
                Text(
                    text = timeText,
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
                            .padding(end = 8.dp),
                        text = targetMessage,
                        style = MyCustomTypography.Medium_14,
                        color = lastMessageColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                MessageStatusIndicator(
                    isMyMessage = isMyMessage,
                    messageStatus = messageStatus,
                    unreadCount = unreadCount
                )
            }
        }
    }
}

@Composable
private fun AvatarWithOnlineIndicator(
    avatarUrl: String?,
    isOnline: Boolean,
    imageLoaderViewModel: ImageLoaderViewModel?
) {
    Box {
        if (avatarUrl != null && imageLoaderViewModel != null) {
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
                contentDescription = "Аватар пользователя"
            )
        } else {
            Image(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Bg_Default_Avatar)
                    .size(60.dp),
                painter = painterResource(id = R.drawable.defaulf_user_avatar),
                contentScale = ContentScale.Crop,
                contentDescription = "Аватар по умолчанию",
            )
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.TopEnd),
            visible = isOnline,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp, end = 3.dp)
                    .clip(CircleShape)
                    .size(12.dp)
                    .background(PrimaryBackground)
                    .padding(2.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Online)
            )
        }
    }
}

/**
 * Компонент для отображения статуса сообщения или счетчика непрочитанных
 */
@Composable
private fun MessageStatusIndicator(
    isMyMessage: Boolean,
    messageStatus: MessageStatus,
    unreadCount: Int
) {
    when {
        !isMyMessage && unreadCount > 0 -> {
            UnreadCountBadge(count = unreadCount)
        }

        isMyMessage -> {
            MyMessageStatusIcon(status = messageStatus)
        }
    }
}

@Composable
private fun UnreadCountBadge(count: Int) {
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
            text = if (count > 99) "99+" else count.toString(),
            style = MyCustomTypography.Bold_8,
            color = Color.White
        )
    }
}

@Composable
private fun MyMessageStatusIcon(status: MessageStatus) {
    when (status) {
        MessageStatus.DELIVERED -> {
            Image(
                modifier = Modifier.size(12.dp),
                painter = painterResource(id = R.drawable.ic_message_delivered),
                contentDescription = "Доставлено",
                colorFilter = ColorFilter.tint(Online)
            )
        }
        MessageStatus.READ -> {
            Image(
                modifier = Modifier.size(14.dp),
                painter = painterResource(id = R.drawable.ic_message_read),
                contentDescription = "Прочитано",
                colorFilter = ColorFilter.tint(Online)
            )
        }
        else -> {}
    }
}

// ============================================
// Preview
// ============================================

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ChatRoomItemContentPreview_MyMessageDelivered() {
    val lastMessageText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = PrimaryPurple)) {
            append("Вы: ")
        }
        append("Привет, как дела?")
    }

    ChatAppTheme {
        ChatRoomItemContent(
            userName = "Александр Чепига",
            avatarUrl = null,
            isOnline = true,
            lastMessageText = lastMessageText,
            lastMessageColor = Color.White.copy(alpha = 0.8f),
            timeText = "5 мин",
            isMyMessage = true,
            messageStatus = MessageStatus.DELIVERED,
            unreadCount = 0,
            imageLoaderViewModel = null,
            onClickChatRoom = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ChatRoomItemContentPreview_MyMessageRead() {
    val lastMessageText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = PrimaryPurple)) {
            append("Вы: ")
        }
        append("Отлично!")
    }

    ChatAppTheme {
        ChatRoomItemContent(
            userName = "Мария Иванова",
            avatarUrl = null,
            isOnline = false,
            lastMessageText = lastMessageText,
            lastMessageColor = Color.White.copy(alpha = 0.5f),
            timeText = "1 час",
            isMyMessage = true,
            messageStatus = MessageStatus.READ,
            unreadCount = 0,
            imageLoaderViewModel = null,
            onClickChatRoom = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ChatRoomItemContentPreview_TheirMessageUnread() {
    val lastMessageText = buildAnnotatedString {
        append("Привет! У меня все отлично, спасибо")
    }

    ChatAppTheme {
        ChatRoomItemContent(
            userName = "Иван Петров",
            avatarUrl = null,
            isOnline = true,
            lastMessageText = lastMessageText,
            lastMessageColor = Color.White.copy(alpha = 0.8f),
            timeText = "Сейчас",
            isMyMessage = false,
            messageStatus = MessageStatus.DELIVERED,
            unreadCount = 3,
            imageLoaderViewModel = null,
            onClickChatRoom = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ChatRoomItemContentPreview_TheirMessageRead() {
    val lastMessageText = buildAnnotatedString {
        append("Хорошо, до встречи!")
    }

    ChatAppTheme {
        ChatRoomItemContent(
            userName = "Анна Смирнова",
            avatarUrl = null,
            isOnline = false,
            lastMessageText = lastMessageText,
            lastMessageColor = Color.White.copy(alpha = 0.8f),
            timeText = "Вчера",
            isMyMessage = false,
            messageStatus = MessageStatus.READ,
            unreadCount = 0,
            imageLoaderViewModel = null,
            onClickChatRoom = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun ChatRoomItemContentPreview_ManyUnread() {
    val lastMessageText = buildAnnotatedString {
        append("Это очень длинное сообщение которое должно обрезаться...")
    }

    ChatAppTheme {
        ChatRoomItemContent(
            userName = "Группа разработчиков",
            avatarUrl = null,
            isOnline = true,
            lastMessageText = lastMessageText,
            lastMessageColor = Color.White.copy(alpha = 0.8f),
            timeText = "10:45",
            isMyMessage = false,
            messageStatus = MessageStatus.DELIVERED,
            unreadCount = 127, // Покажет 99+
            imageLoaderViewModel = null,
            onClickChatRoom = {}
        )
    }
}