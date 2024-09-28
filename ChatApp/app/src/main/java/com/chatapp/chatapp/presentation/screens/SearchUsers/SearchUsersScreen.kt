package com.chatapp.chatapp.presentation.screens.SearchUsers

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.chatapp.chatapp.domain.models.FriendRequest
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.presentation.screens.HomePage.details.SearchTextField
import com.chatapp.chatapp.ui.theme.Bg_Default_Avatar
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.Success
import com.chatapp.chatapp.ui.theme.Surface_Card
import com.chatapp.chatapp.ui.theme.Waiting
import com.chatapp.chatapp.util.CustomSnackBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUsersScreen(
    navController: NavController,
    searchUsersViewModel: SearchUsersViewModel = hiltViewModel(),
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel()
) {

    val searchUserList by searchUsersViewModel.users.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var resultFriendRequest by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                            text = "Search users",
                            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                            fontSize = 28.sp,
                            color = DarkGray_1
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
        },
        snackbarHost = {
            CustomSnackBar(
                snackbarHostState = snackbarHostState,
                isSuccess = resultFriendRequest
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .background(PrimaryBackground),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SearchTextField(
                enabled = true,
                value = searchText,
                onValueChange = {
                    searchText = it
                    if (searchText.isNotEmpty()) searchUsersViewModel.searchUsers(searchText)
                },
                onSeachFieldClick = {}
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(searchUserList, key = { _, user -> user.userId }) { index, user ->
                    SearchUsersItem(
                        user = user,
                        onAddFriendClick = { pendingUser ->
                            friendRequestViewModel.sendFriendRequest(user.userId) { isSuccess ->
                                resultFriendRequest = isSuccess
                                scope.launch {
                                    showSnackbarOnAddFriend(
                                        pendingUser = pendingUser,
                                        isSuccessAddFriend = isSuccess,
                                        snackbarHostState = snackbarHostState
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

suspend fun showSnackbarOnAddFriend(
    pendingUser: User,
    isSuccessAddFriend: Boolean,
    snackbarHostState: SnackbarHostState
){
    if (isSuccessAddFriend){
        snackbarHostState.showSnackbar(
            message = "Friend request sent by ${pendingUser.name.toUpperCase()}",
            actionLabel = "Dismiss",
            duration = SnackbarDuration.Short
        )
    }else{
        snackbarHostState.showSnackbar(
            message = "Friend request has already been sent to ${pendingUser.name.toUpperCase()}",
            actionLabel = "Dismiss",
            duration = SnackbarDuration.Short
        )
    }
}

@Composable
fun SearchUsersItem(
    user: User,
    onAddFriendClick: (User) -> Unit,
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isFriend = if (user.friends.contains(currentUserId)) true else false
    var isRequestSent by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(5.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Surface_Card)
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
            if (isFriend) {
                Text(
                    text = "Friend",
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    color = Success
                )
            } else {
                IconButton(
                    onClick = {
                        onAddFriendClick(user)
                        isRequestSent = true
                    },
                    enabled = !isRequestSent
                ) {
                    Image(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape),
                        painter = painterResource(id = R.drawable.ic_add_friend),
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}


