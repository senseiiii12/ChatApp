package com.chatapp.chatapp.presentation.screens.HomePage.details

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
import com.chatapp.chatapp.domain.models.Message
import com.chatapp.chatapp.domain.models.MessageStatus
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.ui.theme.Bg_Default_Avatar
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.Mark_Message
import com.chatapp.chatapp.ui.theme.Online
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.PrimaryPurple
import com.chatapp.chatapp.ui.theme.Surface_Card
import com.chatapp.chatapp.util.TimeManager
import java.util.Date


@Composable
fun UserListItem(
    modifier: Modifier = Modifier,
    currentUserId: String,
    user: User,
    lastMessage: Message?,
    newMessageCount: Int,
    isOnline: Boolean,
    onClick: () -> Unit
) {
    val timeManager = TimeManager()
    val lastMessageText = if (currentUserId == lastMessage?.userId) "You: ${lastMessage.text}" else "${lastMessage?.text}"
    val lastMessageColor = if (lastMessage?.status?.name.equals("READ")) ChatText else Color.White

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Surface_Card)
            .clickable { onClick() }
            .height(90.dp)
            .padding(10.dp)
    ) {
        Box {
            user.avatar?.let {
                AsyncImage(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(70.dp),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.avatar)
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
                    .size(70.dp),
                painter = painterResource(id = R.drawable.defaulf_user_avatar),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )

            if (isOnline) {
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp, end = 5.dp)
                        .border(1.dp, PrimaryBackground, CircleShape)
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
                .padding(start = 10.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = user.name,
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    color = ChatText
                )
                Text(
                    text = timeManager.getTimeSinceLastMessage(lastMessage),
                    fontSize = 10.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_semibold)),
                    color = PrimaryPurple
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
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_medium)),
                    color = lastMessageColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (newMessageCount > 0 && currentUserId != lastMessage?.userId) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .heightIn(min = 16.dp, max = 18.dp)
                            .widthIn(min = 16.dp, max = 40.dp)
                            .background(PrimaryPurple)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${newMessageCount}",
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
    ChatAppTheme {
        UserListItem(
            currentUserId = "123",
            user = User(
                userId = "123",
                name = "Alexander",
                email = "Free Download Check 134 SVG vector file in monocolor and multicolor type for Sketch",
                password = "123",
                lastSeen = Date(0)
            ),
            onClick = {},
            lastMessage = null,
            isOnline = false,
            newMessageCount = 1
        )
    }
}