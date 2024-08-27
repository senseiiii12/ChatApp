package com.chatapp.chatapp.presentation.screens.Chat.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.Outline_1
import com.chatapp.chatapp.ui.theme.PrimaryPurple
import com.chatapp.chatapp.ui.theme.Surface_Card


@Composable
fun ChatInputField(
    inputMessage: String,
    newMessageText: String,
    isEditing: Boolean,
    onInputMessageChange: (String) -> Unit,
    onNewMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface_Card)
            .padding(vertical = 10.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TextField(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(DarkGray_2)
                .weight(1f),
            value = if (isEditing) newMessageText else inputMessage,
            onValueChange = if (isEditing) onNewMessageChange else onInputMessageChange,
            trailingIcon = {
                if (isEditing) {
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onCancelEdit() },
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = DarkGray_1
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                cursorColor = Outline_1,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            textStyle = TextStyle(
                color = ChatText,
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_medium))
            ),
            placeholder = {
                Text(
                    text = "Type your message",
                    color = DarkGray_1,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_semibold)),
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            )
        )
        IconButton(onClick = onSendMessage) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = PrimaryPurple
            )
        }
    }
}