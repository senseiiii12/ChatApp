package com.chatapp.chatapp.features.chat_rooms.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.Outline_1

@Composable
fun SearchTextField(
    enabled: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    onSeachFieldClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        TextField(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .clickable { onSeachFieldClick() }
                .background(DarkGray_2)
                .weight(1f),
            enabled = enabled,
            value = value,
            onValueChange = onValueChange,
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                    tint = DarkGray_1
                )
            },
            colors = TextFieldDefaults.colors(
                cursorColor = Outline_1,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(
                color = ChatText,
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_medium))
            ),
            placeholder = {
                Text(
                    text = "Search...",
                    color = DarkGray_1,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.gilroy_semibold)),
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            )
        )

    }
}