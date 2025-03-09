package com.chatapp.chatapp.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.Error

@Composable
fun ErrorMessage(
    modifier: Modifier,
    message: String
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = message.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { -it }) + expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        Text(
            modifier = Modifier
                .padding(start = 12.dp, top = 2.dp),
            text = message,
            color = Error,
            fontFamily = FontFamily(Font(R.font.gilroy_semibold)),
            fontSize = 12.sp
        )
    }
}