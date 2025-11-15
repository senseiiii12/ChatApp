package com.buildpc.firstcompose.EnterScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.Outline_1

@Composable
fun EditField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    iconStart: ImageVector,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isPasswordField: Boolean = false
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val visualTransformation = if (isPasswordField && !passwordVisible)
        PasswordVisualTransformation() else visualTransformation

    val trailingIconState = if (passwordVisible)
        painterResource(R.drawable.ic_visibility)
    else
        painterResource(R.drawable.ic_visibility_off)

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(DarkGray_2)
            .height(52.dp),
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
        textStyle = MyCustomTypography.Medium_14.copy(
            color = Color.White,
            letterSpacing = 0.5.sp
        ),
        placeholder = {
            Text(
                text = placeholder,
                style = MyCustomTypography.Medium_14.copy(letterSpacing = 0.5.sp),
                color = Color.White.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = iconStart,
                contentDescription = null,
                tint = Color.White
            )
        },
        trailingIcon = {
            if (isPasswordField) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = trailingIconState,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Preview(showBackground = true)
@Composable
private fun EditFieldPreview() {
    ChatAppTheme {
        EditField(
            value = "12345",
            onValueChange = {},
            placeholder = "Username",
            iconStart = Icons.Default.Person,
            visualTransformation = VisualTransformation.None,
            keyboardType = KeyboardType.Text,
            isPasswordField = true
        )
    }
}