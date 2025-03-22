package com.chatapp.chatapp.features.friend_requests.presentation

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.search_user.presentation.FriendRequestViewModel
import com.chatapp.chatapp.ui.theme.Bg_Default_Avatar
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.Error
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.Success
import com.chatapp.chatapp.ui.theme.SecondaryBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel()
) {


    LaunchedEffect(Unit) {
        friendRequestViewModel.getPendingFriendRequestsWithUserInfo()
    }

    val requestInFriends by friendRequestViewModel.friendRequestAndUserInfo.collectAsState()

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
                itemsIndexed(
                    requestInFriends,
                    key = { _, request -> request.user.userId }) { index, request ->
                    FriendListUsersItem(
                        user = request.user,
                        onAccept = {
                            friendRequestViewModel.respondToFriendRequest(request.friendRequest.id, true){
                                friendRequestViewModel.getPendingFriendRequestsWithUserInfo()
                            }
                        },
                        onDecline = {
                            friendRequestViewModel.respondToFriendRequest(request.friendRequest.id, false){
                                friendRequestViewModel.getPendingFriendRequestsWithUserInfo()
                            }
                        }
                    )
                }
            }
        }

    }
}


@Composable
fun FriendListUsersItem(
    user: User,
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
                user.avatar?.let {
                    AsyncImage(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(30.dp),
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
                        .size(30.dp),
                    painter = painterResource(id = R.drawable.defaulf_user_avatar),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = user.name,
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                color = ChatText
            )
        }
        Row(modifier = Modifier.padding(end = 6.dp)) {
            IconButton(
                modifier = Modifier.size(25.dp),
                onClick = { onAccept() }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Success
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                modifier = Modifier.size(25.dp),
                onClick = { onDecline() }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    tint = Error
                )
            }
        }
    }
}

