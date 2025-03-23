package com.chatapp.chatapp.features.search_user.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.core.presentation.FriendRequestViewModel
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.auth.domain.User
import com.chatapp.chatapp.features.search_user.presentation.details.SearchUsersItem
import com.chatapp.chatapp.features.search_user.presentation.details.TopBarSearchScreen
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.SecondaryBackground
import com.chatapp.chatapp.util.CustomSnackBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUsersScreen(
    navController: NavController,
    searchUsersViewModel: SearchUsersViewModel = hiltViewModel(),
    friendRequestViewModel: FriendRequestViewModel = hiltViewModel(),
    usersViewModel: UsersViewModel
) {

    val searchUserList by searchUsersViewModel.users.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var resultFriendRequest by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val currentUserId = usersViewModel.currentUser.value.userId

    Scaffold(
        topBar = {
            TopBarSearchScreen(
                navController = navController,
                value = searchText,
                onValueChange = {
                    searchText = it
                    if (searchText.isNotEmpty()) searchUsersViewModel.searchUsers(searchText)
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
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(searchUserList, key = { _, user -> user.userId }) { index, user ->
                    SearchUsersItem(
                        user = user,
                        currentUserId = currentUserId,
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
                    Divider(
                        modifier = Modifier.padding(start = 62.dp),
                        color = SecondaryBackground
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



