package com.chatapp.chatapp.features.my_friends.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.chatapp.chatapp.R
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.ui.theme.Bg_Default_Avatar
import com.chatapp.chatapp.ui.theme.Error
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.SecondaryBackground

@Composable
fun MyFriendsScreen(
    navController: NavController,
    viewModel: MyFriendsViewModel = hiltViewModel()
) {
    val state by viewModel.myFriendsState.collectAsState()
    val friendsList = state.data ?: emptyList()

    Scaffold(
        topBar = {
            MyFriendTopBar(
                navController = navController,
                value = "",
                onValueChange = { }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PrimaryBackground),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(color = Color.White)
                }

                friendsList.isEmpty() -> {
                    EmptyScreen()
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(friendsList) { friend ->
                            MyFriendItem(
                                friend = friend,
                                onDeleteClick = viewModel::deleteFriend
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFriendTopBar(
    navController: NavController,
    value: String,
    onValueChange: (String) -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SecondaryBackground
        ),
        title = {
//            SearchTextField(
//                value = value,
//                onValueChange = onValueChange,
//            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Friends",
                    style = MyCustomTypography.Bold_24,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
    )
}

@Composable
fun EmptyScreen(modifier: Modifier = Modifier) {
    Text(
        text = "No friends",
        style = MyCustomTypography.Medium_14,
        color = Color.White.copy(alpha = 0.5f)
    )
}

@Composable
fun MyFriendItem(
    friend: User,
    onDeleteClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PrimaryBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            friend.avatar?.let { avatar ->
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
            Column {
                Text(
                    text = friend.name,
                    style = MyCustomTypography.Bold_14,
                    color = Color.White,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = friend.email,
                    style = MyCustomTypography.Medium_12,
                    color = Color.White.copy(alpha = 0.5f),
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { expanded = true }) {
                    Image(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Default.MoreVert,
                        colorFilter = ColorFilter.tint(Color.White),
                        contentDescription = null
                    )
                }
                DropdownMenu(
                    modifier = Modifier
                        .background(SecondaryBackground)
                        .width(IntrinsicSize.Min)
                        .height(IntrinsicSize.Min),
                    expanded = expanded,
                    offset = DpOffset(x = -50.dp, y = -20.dp),
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.background(SecondaryBackground),
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.user_delete),
                                contentDescription = "Delete Friend",
                                tint = Error
                            )
                        },
                        text = {
                            Text(
                                modifier = Modifier.wrapContentWidth(),
                                text = "Delete",
                                style = MyCustomTypography.SemiBold_12,
                                color = Error
                            )
                        },
                        onClick = {
                            expanded = false
                            onDeleteClick(friend.userId)
                        }
                    )
                }
            }
        }
    }
}