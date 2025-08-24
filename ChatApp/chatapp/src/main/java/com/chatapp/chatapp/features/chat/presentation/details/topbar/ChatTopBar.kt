package com.chatapp.chatapp.features.chat.presentation.details.topbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chatapp.chatapp.R
import com.chatapp.chatapp.features.chat.domain.Message
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.ui.theme.Bg_Default_Avatar
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.Online
import com.chatapp.chatapp.ui.theme.SecondaryBackground
import com.chatapp.chatapp.util.TimeManager
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    otherUser: User,
    stateTopMenu: State<TopMenuState>,
    onBack: () -> Unit,
    onCloseMenu: () -> Unit,
    onDeleteMessage: (List<Message>) -> Unit,
    onEditMessage: (String, Message?) -> Unit,
    onCopyMessage: (List<Message>) -> Unit,
    usersViewModel: UsersViewModel
) {

    val topMenuState = stateTopMenu.value

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SecondaryBackground
        ),
        title = {
            AnimatedContent(targetState = topMenuState.isOpenTopMenu) { stateTopMenuMessage ->
                if (!stateTopMenuMessage) {
                    ChatHeader(
                        otherUser = otherUser,
                        usersViewModel = usersViewModel
                    )
                } else {
                    TopMenuSelectedMessage(
                        stateTopMenu = stateTopMenu,
                        onDeleteMessage = {
                            onDeleteMessage(topMenuState.listSelectedMessages)
                        },
                        onEditMessage = {
                            onEditMessage(
                                topMenuState.listSelectedMessages.first().text,
                                topMenuState.listSelectedMessages.first()
                            )
                        } ,
                        onCopyMessage = {
                            onCopyMessage(topMenuState.listSelectedMessages)
                        }
                    )
                }
            }
        },
        navigationIcon = {
            AnimatedContent(targetState = topMenuState.isOpenTopMenu) { stateTopMenuMessage ->
                if (!stateTopMenuMessage) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                } else {
                    IconButton(onClick = onCloseMenu) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun OnlineStatus(
    otherUser: User,
    usersViewModel: UsersViewModel
) {

    val isOnline by usersViewModel.userStatuses.collectAsState()
    val onlineStatus = isOnline[otherUser.userId]?.first ?: false
    val lastSeenStatus = isOnline[otherUser.userId]?.second ?: otherUser.lastSeen


    AnimatedContent(targetState = onlineStatus) { isOnline ->
        if (isOnline) {
            Row(
                modifier = Modifier.padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(8.dp)
                        .background(Online)
                )
                Text(
                    modifier = Modifier.padding(start = 5.dp),
                    text = "Online",
                    style = MyCustomTypography.Normal_12,
                    color = Online,
                )
            }
        } else {
            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = TimeManager.formatLastSeenDate(lastSeenStatus),
                style = MyCustomTypography.Normal_12,
                color = Color.White.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
fun ChatHeader(
    otherUser: User,
    usersViewModel: UsersViewModel
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        otherUser.avatar?.let {
            AsyncImage(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(30.dp),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(otherUser.avatar)
                    .crossfade(true)
                    .diskCacheKey(otherUser.avatar)
                    .memoryCacheKey(otherUser.avatar)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
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
        Column {
            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = otherUser.name,
                style = MyCustomTypography.Bold_20,
                color = Color.White,
            )
            OnlineStatus(
                otherUser = otherUser,
                usersViewModel = usersViewModel
            )
        }
    }
}
