package com.buildpc.firstcompose.EnterScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.Outline_1


@Composable
fun EditField(
    placeholder: String,
    iconStart: ImageVector,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation,
    onValueChange:(String) -> Unit,
    value: String,
) {

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(DarkGray_2)
            .height(48.dp),
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        colors = TextFieldDefaults.colors(
            cursorColor = Outline_1,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        textStyle = TextStyle(
            color = Color.White,
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.gilroy_medium))
        ),
        placeholder = {
            Text(
                text = placeholder,
                color = ChatText,
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.gilroy_medium))
            )
        },
        leadingIcon = {
            Icon(
                imageVector = iconStart,
                contentDescription = null,
                tint = Color.White
            )
        },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        )
    )
}
//
//@Preview(showBackground = true, backgroundColor = 0xFF000000)
//@Composable
//private fun EditFieldPreview() {
//    ChatAppTheme {
//        EditField(
//            placeholder = "Username or Email",
//            iconStart = Icons.Default.Person,
//            visualTransformation = VisualTransformation.None,
//            keyboardType = KeyboardType.Text,
//            onValueChange = {},
//            value = ""
//        )
//    }
//}