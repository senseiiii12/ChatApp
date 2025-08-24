package com.chatapp.chatapp.features.chat_rooms.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.SecondaryBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarChatsRoom(
    onSearchButtonClick: () -> Unit,
    onMenuButtonClick: () -> Unit,
) {

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            SecondaryBackground
        ),
        navigationIcon = {
            IconButton(
                onClick = onMenuButtonClick
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    tint = Color.White.copy(alpha = 0.5f),
                    contentDescription = null
                )
            }
        },
        actions = {
            IconButton(
                onClick = onSearchButtonClick
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    tint = Color.White.copy(alpha = 0.5f),
                    contentDescription = null
                )
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chats",
                    style = MyCustomTypography.Bold_24,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    )

}

@Preview
@Composable
private fun TopBarHomePreview() {
    ChatAppTheme {
        TopBarChatsRoom(
            onSearchButtonClick = {},
            onMenuButtonClick = {}
        )
    }
}