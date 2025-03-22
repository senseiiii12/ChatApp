package com.chatapp.chatapp.features.search_user.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
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
        }
    )
}

