package com.chatapp.chatapp.presentation.screens.Chat.details.topbar

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R

@Composable
fun TopMenuSelectedMessage(
    stateTopMenu: TopMenuState,
    countSelectedMessage: Int,
    onDeleteMessage: () -> Unit,
    onEditMessage: () -> Unit,
    onCopyMessage: () -> Unit,
    ) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = countSelectedMessage.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontFamily = Font(R.font.gilroy_semibold).toFontFamily()
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = stateTopMenu.countSelectedMessage > 1,
                contentAlignment = Alignment.Center
            ) {isVisible->
                if (!isVisible){
                    IconButton(onClick = onEditMessage) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit_message),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
            IconButton(onClick = onDeleteMessage) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_trash),
                    contentDescription = null,
                    tint = Color.White
                )
            }
            IconButton(onClick = onCopyMessage) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_copy),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}


//@Preview
//@Composable
//private fun EditMessageTopMenuPreview() {
//    ChatAppTheme {
//        TopMenuSelectedMessage(
//            countSelectedMessage = 6,
//            onEditMessage = {},
//            onDeleteMessage = {},
//            onCopyMessage = {}
//        )
//    }
//}