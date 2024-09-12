package com.chatapp.chatapp.presentation.screens.HomePage.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.DarkGray_1

@Composable
fun TopBarHome(
    onSettingsClick:() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Messages",
            fontFamily = FontFamily(Font(R.font.gilroy_bold)),
            fontSize = 28.sp,
            color = DarkGray_1
        )
        IconButton(
            onClick = onSettingsClick
        ) {
            Icon(
                modifier = Modifier.size(25.dp),
                painter = painterResource(id = R.drawable.filter),
                tint = DarkGray_1,
                contentDescription = null
            )
        }
    }
}