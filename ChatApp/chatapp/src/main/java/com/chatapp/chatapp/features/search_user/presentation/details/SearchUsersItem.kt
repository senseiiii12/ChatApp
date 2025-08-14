package com.chatapp.chatapp.features.search_user.presentation.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chatapp.chatapp.R
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.friend_requests.presentation.AnimatedOutlinedButton
import com.chatapp.chatapp.ui.theme.Bg_Default_Avatar
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.Green100
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import java.util.Date


@Composable
fun SearchUsersItem(
    user: User,
    currentUserId: String,
    haveIncomingRequest: Boolean,
    haveOutgoingRequest: Boolean,
    canSendRequest: Boolean,
    onAddFriend: (User) -> Unit,
    onAcceptFriend: () -> Unit,
    onWriteMessage: (User) -> Unit,
) {
    val isFriend = user.friends.contains(currentUserId)
    var isRequestSent by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            user.avatar?.let { avatar ->
                AsyncImage(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(42.dp),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatar)
                        .crossfade(true)
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
                    .size(42.dp),
                painter = painterResource(id = R.drawable.defaulf_user_avatar),
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user.name,
                style = MyCustomTypography.Bold_14,
                color = Color.White,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onWriteMessage(user) }) {
                    Image(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(id = R.drawable.ic_write_message),
                        colorFilter = ColorFilter.tint(Color.White),
                        contentDescription = null
                    )
                }

                when {
                    isFriend -> {
                        AnimatedOutlinedButton(
                            modifier = Modifier.height(30.dp),
                            text = "Friend",
                            enabled = false,
                            borderColor = Color.Transparent,
                            containerColor = Green100.copy(alpha = 0.2f),
                            textColor = Green100,
                            style = MyCustomTypography.Medium_12,
                            onClick = {}
                        )
                    }
                    canSendRequest -> {
                        if (!isRequestSent){
                            AnimatedOutlinedButton(
                                modifier = Modifier.height(30.dp),
                                text = "Add Friend",
                                borderColor = Green100,
                                containerColor = Green100.copy(alpha = 0.2f),
                                textColor = Green100,
                                style = MyCustomTypography.Medium_12,
                                onClick = {
                                    onAddFriend(user)
                                    isRequestSent = true
                                }
                            )
                        }else{
                            AnimatedOutlinedButton(
                                modifier = Modifier.height(30.dp),
                                text = "Request sent",
                                enabled = false,
                                borderColor = Color.Yellow,
                                containerColor = Color.Yellow.copy(alpha = 0.2f),
                                textColor = Color.Yellow,
                                style = MyCustomTypography.Medium_12,
                                onClick = {}
                            )
                        }
                    }
                    haveIncomingRequest -> {
                        AnimatedOutlinedButton(
                            modifier = Modifier.height(30.dp),
                            text = "Accept as friend",
                            borderColor = Green100,
                            containerColor = Green100.copy(alpha = 0.2f),
                            textColor = Green100,
                            style = MyCustomTypography.Medium_12,
                            onClick = { onAcceptFriend() }
                        )
                    }
                    haveOutgoingRequest -> {
                        AnimatedOutlinedButton(
                            modifier = Modifier.height(30.dp),
                            text = "Request sent",
                            enabled = false,
                            borderColor = Color.Yellow,
                            containerColor = Color.Yellow.copy(alpha = 0.3f),
                            textColor = Color.Yellow,
                            style = MyCustomTypography.Medium_12,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun SearchUsersItemPreview() {
    val testUser = User(
        userId = "123",
        name = "Alexander",
        email = "",
        password = "123",
        lastSeen = Date(0),
        friends = listOf("1230")
    )
    ChatAppTheme {
        SearchUsersItem(
            user = testUser,
            currentUserId = "123",
            haveIncomingRequest = true,
            haveOutgoingRequest = false,
            canSendRequest = false,
            onAddFriend = {},
            onAcceptFriend = {},
            onWriteMessage = {},
        )
    }
}