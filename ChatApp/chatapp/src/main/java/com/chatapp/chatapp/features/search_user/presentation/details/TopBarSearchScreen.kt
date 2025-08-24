package com.chatapp.chatapp.features.search_user.presentation.details

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.chatapp.chatapp.R
import com.chatapp.chatapp.features.navigation.Route
import com.chatapp.chatapp.ui.theme.SecondaryBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarSearchScreen(
    navController: NavController,
    value: String,
    onValueChange: (String) -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SecondaryBackground
        ),
        title = {
            SearchTextField(
                value = value,
                onValueChange = onValueChange,
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = { navController.navigate(Route.MyFriends.route) }) {
                Icon(
                    painter = painterResource(R.drawable.my_friends),
                    contentDescription = "Friend requests",
                    tint = Color.White
                )
            }
            IconButton(onClick = { navController.navigate(Route.FriendsRequests.route) }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Friend requests",
                    tint = Color.White
                )
            }
        }
    )
}

