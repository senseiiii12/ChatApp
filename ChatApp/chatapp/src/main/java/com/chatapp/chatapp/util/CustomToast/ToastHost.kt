package com.chatapp.chatapp.util.CustomToast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.Surface_Card
import kotlinx.coroutines.delay


@Composable
fun ToastHost(toastState: ToastState) {
    if (toastState.isToastVisible) {
        LaunchedEffect(Unit) {
            delay(toastState.durationMillis)
            toastState.hideToast()
        }

        Popup(alignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .padding(bottom = 100.dp)
                    .widthIn(1.dp,150.dp)
                    .heightIn(10.dp,30.dp)
                    .background(Surface_Card, shape = RoundedCornerShape(10.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = toastState.message,
                    color = ChatText,
                    fontSize = 10.sp,
                    fontFamily = Font(R.font.gilroy_medium).toFontFamily(),
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}