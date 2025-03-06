package com.chatapp.chatapp.presentation.screens.Chat.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chatapp.chatapp.R
import com.chatapp.chatapp.domain.models.Message
import com.chatapp.chatapp.domain.models.MessageStatus
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.ui.theme.Bg_Default_Avatar
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.Mark_Message
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.PrimaryPurple
import com.chatapp.chatapp.ui.theme.Surface_Card
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    message: Message,
    isCurrentUser: Boolean,
    currentUser: User,
    otherUser: User,
    isEditing: Boolean,
    status: MessageStatus,
    onOpenTopMenu: (Message) -> Unit,
) {

    val currentUserColor = remember {
        Brush.linearGradient(
            listOf(
                PrimaryPurple.copy(alpha = 0.8f),
                PrimaryPurple.copy(alpha = 0.5f)
            )
        )
    }
    val otherUserColor = remember {
        Brush.linearGradient(listOf(Surface_Card.copy(alpha = 0.8f), Surface_Card))
    }
    val backgroundColor = remember { if (isCurrentUser) currentUserColor else otherUserColor }
    val horizontalArrangement = remember { if (isCurrentUser) Arrangement.End else Arrangement.Start }
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val selectedColorMessage by animateColorAsState(
        targetValue = if (isEditing) Surface_Card else PrimaryBackground,
        animationSpec = tween(durationMillis = 100)
    )


    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(selectedColorMessage)
            .padding(vertical = 4.dp),
        horizontalArrangement = horizontalArrangement
    ) {
        if (!isCurrentUser) {
            UserAvatar(otherUser.avatar)
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .widthIn(min = 20.dp, max = (screenWidth * 0.7).dp)
                .background(backgroundColor)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { if (isCurrentUser) onOpenTopMenu(message) }
                )
                .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 6.dp)
                .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)),
        ) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_semibold)),
                color = ChatText,
            )
            Row(
                modifier = Modifier.align(Alignment.End),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier,
                    text = formatTimestampToDate(Date(message.timestamp)),
                    fontSize = 8.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                    color = ChatText.copy(alpha = 0.6f),
                )
                Spacer(modifier = Modifier.width(4.dp))
                if(isCurrentUser){
                    when (status) {
                        MessageStatus.DELIVERED -> {
                            Icon(
                                modifier = Modifier.size(12.dp),
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Mark_Message
                            )
                        }
                        MessageStatus.READ -> {
                            Image(
                                modifier = Modifier.size(12.dp),
                                painter = painterResource(id = R.drawable.double_check_icon),
                                contentDescription = null,
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
        if (isCurrentUser) {
            UserAvatar(currentUser.avatar)
        }
    }
}


@Composable
fun UserAvatar(avatar: String?) {
    when (avatar) {
        null -> {
            Image(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Bg_Default_Avatar)
                    .size(30.dp),
                painter = painterResource(id = R.drawable.defaulf_user_avatar),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        }

        else -> {
            AsyncImage(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(30.dp),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(avatar)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
        }
    }
}


@Composable
fun ToolTipMenu(
    onDelete: () -> Unit,
    onEditMessage: () -> Unit
) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max).padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onEditMessage() }
                .padding(vertical = 2.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(16.dp),
                painter = painterResource(id = R.drawable.ic_edit),
                contentDescription = null,
            )
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = "Edit",
                fontSize = 10.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                color = ChatText
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onDelete() }
                .padding(vertical = 2.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.size(16.dp),
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = null,
            )
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = "Delete",
                fontSize = 10.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                color = ChatText
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//private fun MessageItemPreview() {
//    ChatAppTheme {
//        MessageItem(
//            message = Message(
//                userId = "BIsXDQm57KgWPqgYIhzUwXnfBkZ2",
//                text = "Hello",
//                timestamp = Date(0),
//                messageId = "b8224c6e-9786-4cf8-8692-5189e47c1b7d"
//            ),
//            isCurrentUser = false,
//            otherUser = User(),
//            isEditing = false,
//            status = MessageStatus.READ,
//            onDelete = {},
//            onEditMessage = {},
//        )
//    }
//}


fun formatTimestampToDate(date: Date): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}

