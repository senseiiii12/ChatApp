package com.chatapp.chatapp.presentation.screens.Chat.details

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.R
import com.chatapp.chatapp.domain.models.User
import com.chatapp.chatapp.presentation.screens.HomePage.UsersViewModel
import com.chatapp.chatapp.ui.theme.ChatText
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.Online
import com.chatapp.chatapp.ui.theme.Surface_Card
import com.chatapp.chatapp.util.TimeLastMessage
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    otherUser: User,
    navController: NavController,
    usersViewModel: UsersViewModel = hiltViewModel()
) {


    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Surface_Card
        ),
        title = {
            Row (verticalAlignment = Alignment.CenterVertically){
                Image(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Cyan)
                        .size(30.dp),
                    painter = painterResource(id = R.drawable.avatar_image),
                    contentDescription = null,
                )
                Column {
                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = otherUser.name,
                        fontSize = 20.sp,
                        color = ChatText,
                        fontFamily = FontFamily(Font(R.font.gilroy_bold)),
                    )
                    if (otherUser.online){
                        Row (modifier = Modifier.padding(start = 10.dp),
                            verticalAlignment = Alignment.CenterVertically){
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(8.dp)
                                    .background(Online)
                            )
                            Text(
                                modifier = Modifier.padding(start = 5.dp),
                                text = "Online",
                                fontSize = 10.sp,
                                color = Online,
                                fontFamily = FontFamily(Font(R.font.gilroy_semibold)),
                            )
                        }
                    }else{
                        Text(
                            modifier = Modifier.padding(start = 10.dp),
                            text = formatLastSeenDate(otherUser.lastSeen),
                            fontSize = 12.sp,
                            color = DarkGray_1,
                            fontFamily = FontFamily(Font(R.font.gilroy_semibold)),
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }
    )
}

fun formatLastSeenDate(lastSeen: Date): String {
    // Форматирование для времени
    val timeFormat = SimpleDateFormat("HH:mm", Locale("ru"))
    timeFormat.timeZone = TimeZone.getDefault()

    // Форматирование для даты и времени
    val dateTimeFormat = SimpleDateFormat("d MMMM yyyy 'в' HH:mm", Locale("ru"))
    dateTimeFormat.timeZone = TimeZone.getDefault()

    // Форматирование для даты
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getDefault()

    val currentDate = Date()
    val lastSeenDateString = dateFormat.format(lastSeen)
    val currentDateString = dateFormat.format(currentDate)

    return if (lastSeenDateString == currentDateString) {
        "Last seen ${timeFormat.format(lastSeen)}"
    } else {
        "Last seen ${dateTimeFormat.format(lastSeen)}"
    }
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    fmt.timeZone = TimeZone.getDefault()
    return fmt.format(date1) == fmt.format(date2)
}