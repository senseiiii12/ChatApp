package com.buildpc.firstcompose.EnterScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.Outline_1
import com.chatapp.chatapp.ui.theme.Surface_2


@Composable
fun EditField(
    placeholder: String,
    iconStart: ImageVector,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation,
    onValueChange:(String) -> Unit,
    value: String
) {


    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Outline_1, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Surface_2)
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
                color = Color.White.copy(alpha = 0.7f),
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

//@Preview(showBackground = true, backgroundColor = 0xFF000000)
//@Composable
//private fun EditFieldPreview() {
//    ChatAppTheme {
//        EditField(
//            placeholder = "Username or Email",
//            iconStart = Icons.Default.Person,
//            visualTransformation = VisualTransformation.None,
//            keyboardType = KeyboardType.Text
//        )
//    }
//}