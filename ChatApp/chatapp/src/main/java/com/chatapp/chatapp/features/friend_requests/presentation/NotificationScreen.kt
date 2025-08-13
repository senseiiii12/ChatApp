package com.chatapp.chatapp.features.friend_requests.presentation

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chatapp.chatapp.R
import com.chatapp.chatapp.core.presentation.FriendRequestViewModel
import com.chatapp.chatapp.ui.theme.Bg_Default_Avatar
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.Error
import com.chatapp.chatapp.ui.theme.Green100
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.SecondaryBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel()
) {

    val requestInFriendsState by friendRequestViewModel.friendRequestsState.collectAsState()
    Log.d("requestInFriends",requestInFriendsState.toString())

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBackground
                ),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notification",
                            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                            fontSize = 28.sp,
                            color = DarkGray_1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .background(PrimaryBackground),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    requestInFriendsState.requestsInFriendItemData,
                    key = { it.request.id }) { item ->
                    FriendListUsersItem(
                        itemState = item,
                        onAccept = {
                            friendRequestViewModel.respondToFriendRequest(
                                item.request,
                                true
                            )
                        },
                        onDecline = {
                            friendRequestViewModel.respondToFriendRequest(
                                item.request,
                                false
                            )
                        }
                    )
                }
            }
        }

    }
}


@Composable
fun FriendListUsersItem(
    itemState: RequestsInFriendItemState,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(SecondaryBackground)
            .clickable { }
            .height(50.dp)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                itemState.user.avatar?.let {
                    AsyncImage(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(30.dp),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(itemState.user.avatar)
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
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = itemState.user.name,
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                color = ChatText
            )
        }
        // Кнопки
        Row(modifier = Modifier.padding(end = 6.dp)) {
            // Accept
            OutlinedButton(
                onClick = onAccept,
                enabled = !itemState.isLoadingAccept && !itemState.isSuccessAccept,
                border = BorderStroke(1.dp, Green100.copy(alpha = 0.5f))
            ) {
                when {
                    itemState.isLoadingAccept -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    else -> {
                        Text(
                            text = "Accept",
                            style = MyCustomTypography.Medium_12,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))


            OutlinedButton(
                onClick = onDecline,
                enabled = !itemState.isLoadingDecline && !itemState.isSuccessDecline,
                border = BorderStroke(1.dp, Error.copy(alpha = 0.5f)),
            ) {
                AnimatedContent(
                    targetState = itemState.isLoadingDecline,
                ) { isLoading ->
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Decline",
                            style = MyCustomTypography.Medium_12,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

