package com.chatapp.chatapp.presentation.screens.HomePage

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.R
import com.chatapp.chatapp.presentation.navigation.Route
import com.chatapp.chatapp.presentation.screens.Chat.ChatViewModel
import com.chatapp.chatapp.presentation.screens.HomePage.details.SearchTextField
import com.chatapp.chatapp.presentation.screens.HomePage.details.TopBarHome
import com.chatapp.chatapp.presentation.screens.HomePage.details.UserListItemShimmerEffect
import com.chatapp.chatapp.presentation.screens.HomePage.details.UserList
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.Outline_1
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson



@Composable
fun HomePage(navController: NavController) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(PrimaryBackground)

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val usersViewModel: UsersViewModel = hiltViewModel()
    val chatViewModel: ChatViewModel = hiltViewModel()
    val usersState = usersViewModel.users.collectAsState()

    val filteredUsers = usersState.value.isSuccess.filter { it.userId != currentUserId }
    val currentUser = usersState.value.isSuccess.find { it.userId == currentUserId }

    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        usersViewModel.getUsers()
    }

    LaunchedEffect(filteredUsers) {
        val chatIds = filteredUsers.map { user ->
            val otherUserId = user.userId
            if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"
        }
        chatViewModel.updateChatIds(chatIds)
        chatViewModel.startListeningToChats()
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        TopBarHome (
            onSettingsClick = { }
        )
        Spacer(modifier = Modifier.height(16.dp))
        SearchTextField(
            value = searchText,
            onValueChange = { searchText = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (usersState.value.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            repeat(6) {
                UserListItemShimmerEffect(
                    state = usersState.value
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        UserList(
            stateUserList = usersState.value,
            filteredUsers = filteredUsers,
            onUserClick = { user ->
                val otherUserJson = Gson().toJson(user)
                val currentUserJson = Gson().toJson(currentUser)
                navController.navigate("chat/${Uri.encode(otherUserJson)}/${Uri.encode(currentUserJson)}")
            }
        )
        Button(onClick = {
            usersViewModel.updateUserStatus(currentUserId, false)
            FirebaseAuth.getInstance().signOut()
            navController.navigate(Route.MainEntrance.route)
        }) {
            Text(text = "${currentUser?.name}")
        }
    }


}



