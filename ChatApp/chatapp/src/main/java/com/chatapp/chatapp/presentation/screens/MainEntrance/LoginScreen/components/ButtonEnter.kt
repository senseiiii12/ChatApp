package com.buildpc.firstcompose.EnterScreen.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.PrimaryPurple


@Composable
fun ButtonEnter(
    background: Color = PrimaryPurple,
    text: String,
    textColor: Color = Color.White,
    borderColor: Color = Color.Transparent,
    OnClick:() -> Unit
) {

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
            .clip(RoundedCornerShape(100.dp))
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = background
        ),
        onClick = OnClick
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontFamily = FontFamily(Font(R.font.gilroy_semibold))
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonEnterPreview() {
    ChatAppTheme {
        ButtonEnter(
            text = "123",
            OnClick = {}
        )
    }
}