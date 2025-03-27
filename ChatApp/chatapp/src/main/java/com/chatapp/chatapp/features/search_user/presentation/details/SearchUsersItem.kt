package com.chatapp.chatapp.features.search_user.presentation.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    onAddFriend: (User) -> Unit,
    onWriteMessage: (User) -> Unit,
) {

    val isFriend = if (user.friends.contains(currentUserId)) true else false
    var isRequestSent by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryBackground)
            .clickable { }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box {
                user.avatar?.let {
                    AsyncImage(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(42.dp),
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
                        .size(42.dp),
                    painter = painterResource(id = R.drawable.defaulf_user_avatar),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier,
                text = user.name,
                style = MyCustomTypography.Bold_14,
                color = Color.White,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.size(30.dp),
                    onClick = {onWriteMessage(user)}
                ) {
                    Image(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(id = R.drawable.ic_write_message),
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.White),
                        contentDescription = null,
                    )
                }
                if (isFriend) {
                    IconButton(
                        modifier = Modifier.size(30.dp),
                        onClick = {},
                        enabled = false
                    ) {
                        Image(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_current_friend),
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.tint(Green100),
                            contentDescription = null,
                        )
                    }
                } else {
                    IconButton(
                        modifier = Modifier.size(30.dp),
                        onClick = {
                            onAddFriend(user)
                            isRequestSent = true
                        },
                        enabled = !isRequestSent
                    ) {
                        Image(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(id = R.drawable.ic_add_user),
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.tint(Color.White),
                            contentDescription = null,
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
        name = "Александр Чепига",
        email = "",
        password = "123",
        lastSeen = Date(0),
        friends = listOf("1230")
    )
    ChatAppTheme {
        SearchUsersItem(
            user = testUser,
            currentUserId = "123",
            onAddFriend = {},
            onWriteMessage = {}
        )
    }
}