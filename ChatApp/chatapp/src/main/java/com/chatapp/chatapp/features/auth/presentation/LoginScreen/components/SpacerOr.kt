package com.buildpc.firstcompose.EnterScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.ChatAppTheme
import com.chatapp.chatapp.ui.theme.Outline_1

@Composable
fun SpacerOr() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        Box(
            modifier = Modifier
                .height(1.dp)
                .width(40.dp)
                .weight(0.5f)
                .background(Outline_1)
        )
        Text(
            modifier = Modifier.padding(horizontal = 10.dp),
            text = "Or",
            color = Color.White,
            fontFamily = FontFamily(Font(R.font.poppins_medium)),
            fontSize = 14.sp
        )
        Box(
            modifier = Modifier
                .height(1.dp)
                .width(40.dp)
                .weight(0.5f)
                .background(Outline_1)
        )
        Spacer(modifier = Modifier.weight(0.3f))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SpacerOrPreview() {
    ChatAppTheme {
        SpacerOr()
    }
}