package com.chatapp.chatapp.features.search_user.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.Outline_1
import com.chatapp.chatapp.ui.theme.PrimaryBackground

@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
    ) {
        TextField(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(PrimaryBackground)
                .weight(1f)
                .height(46.dp),
            value = value,
            onValueChange = onValueChange,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f)
                )
            },
            colors = TextFieldDefaults.colors(
                cursorColor = Color.White.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            textStyle = MyCustomTypography.SemiBold_12.copy(color = Color.White),
            placeholder = {
                Text(
                    text = "Search",
                    style = MyCustomTypography.SemiBold_12,
                    color = Color.White.copy(alpha = 0.5f),
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            )
        )

    }
}

@Preview
@Composable
private fun SearchTextFieldPreview() {
    ChatAppTheme {
        SearchTextField(
            value = "",
            onValueChange = {},
        )
    }
}