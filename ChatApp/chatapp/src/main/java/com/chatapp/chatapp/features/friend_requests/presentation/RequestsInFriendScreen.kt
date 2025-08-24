package com.chatapp.chatapp.features.friend_requests.presentation

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.text.TextStyle
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
import com.chatapp.chatapp.ui.theme.Surface_2

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun RequestsInFriendScreen(
    navController: NavController,
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        friendRequestViewModel.getPendingFriendRequestsWithUserInfo()
    }

    val requestInFriendState by friendRequestViewModel.friendRequestsState.collectAsState()
    Log.d("requestInFriends", requestInFriendState.toString())

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
                            text = "Friends request",
                            style = MyCustomTypography.Bold_24,
                            color = Color.White.copy(alpha = 0.5f)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (requestInFriendState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.5f)
                )
            } else if (requestInFriendState.requestsInFriendItemData.isEmpty()) {
                EmptyScreen()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        requestInFriendState.requestsInFriendItemData,
                        key = { it.request.id }) { item ->
                        FriendListUsersItem(
                            modifier = Modifier.animateItem(),
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
}

@Composable
fun EmptyScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Friend request is empty",
        style = MyCustomTypography.SemiBold_16,
        color = Surface_2
    )
}


@Composable
fun FriendListUsersItem(
    modifier: Modifier,
    itemState: RequestsInFriendItemState,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp)
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
                style = MyCustomTypography.Medium_14,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
        Row(modifier = Modifier.padding(end = 8.dp)) {
            AnimatedOutlinedButton(
                modifier = Modifier.height(30.dp),
                text = "Accept",
                isLoading = itemState.isLoadingAccept,
                enabled = true,
                borderColor = Green100,
                containerColor = Green100.copy(alpha = 0.1f),
                textColor = Green100,
                style = MyCustomTypography.Medium_12,
                onClick = onAccept
            )
            Spacer(modifier = Modifier.width(8.dp))
            AnimatedOutlinedButton(
                modifier = Modifier.height(30.dp),
                text = "Decline",
                isLoading = itemState.isLoadingDecline,
                enabled = true,
                borderColor = Error,
                containerColor = Error.copy(alpha = 0.1f),
                textColor = Error,
                style = MyCustomTypography.Medium_12,
                onClick = onDecline
            )

        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedOutlinedButton(
    modifier: Modifier = Modifier,
    text: String,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    borderColor: Color = Color.Gray,
    containerColor: Color = Color.Transparent,
    textColor: Color = Color.White,
    style: TextStyle,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) with fadeOut(animationSpec = tween(200))
            }
        ) { loading ->
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 1.dp,
                    color = Color.White
                )
            } else {
                Text(
                    text = text,
                    style = style,
                    color = textColor
                )
            }
        }
    }
}


