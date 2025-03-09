package com.chatapp.chatapp.presentation.screens.Chat.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.ChatAppTheme
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
    currentEditMessage: String,
    onInputMessageChange: (String) -> Unit,
    onNewMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onCancelEdit: () -> Unit
) {

    Column {
        AnimatedVisibility(visible = isEditing) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface_Card)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(id = R.drawable.ic_edit_message),
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .width(IntrinsicSize.Min),
                            text = currentEditMessage,
                            fontFamily = Font(R.font.gilroy_medium).toFontFamily(),
                            fontSize = 10.sp,
                            color = Outline_1,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(26.dp),
                        onClick = onCancelEdit
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Outline_1
                        )
                    }
                }
                Divider(color = DarkGray_1)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextField(
                modifier = Modifier
                    .background(Surface_Card)
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                value = if (isEditing) newMessageText else inputMessage,
                onValueChange = if (isEditing) onNewMessageChange else onInputMessageChange,
                trailingIcon = {
                    AnimatedContent(targetState = isEditing) { isEditing->
                        if (!isEditing){
                            IconButton(onClick = onSendMessage) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = PrimaryPurple
                                )
                            }
                        } else{
                            IconButton(onClick = onSendMessage) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Edit",
                                    tint = PrimaryPurple
                                )
                            }
                        }
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
                        text = "Message",
                        color = DarkGray_1,
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.gilroy_semibold)),
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )
        }
    }
}

@Preview
@Composable
private fun ChatInputFieldPreview() {
    ChatAppTheme {
        ChatInputField(
            inputMessage = "",
            newMessageText = "",
            isEditing = false,
            currentEditMessage = "",
            onInputMessageChange = {} ,
            onNewMessageChange = {},
            onSendMessage = { /*TODO*/ }) {
        }
    }
}