package com.chatapp.chatapp.presentation.screens.Chat.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.DarkGray_2
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun DateSeparator(timestamp: Date) {
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH) }
    val dateText = remember { dateFormat.format(timestamp) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = DarkGray_2
        )
        Text(
            text = dateText,
            modifier = Modifier.padding(horizontal = 8.dp),
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            color = DarkGray_1,
            fontSize = 10.sp
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = DarkGray_2,
        )
    }
}