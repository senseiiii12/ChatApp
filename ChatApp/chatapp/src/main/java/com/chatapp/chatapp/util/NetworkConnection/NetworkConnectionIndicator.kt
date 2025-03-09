package com.chatapp.chatapp.util.NetworkConnection

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.DarkGray_1

@Composable
fun NetworkConnectionIndicator(){
    val connectionNetworkState by rememberConnectivityState()

    val isConnected by remember(connectionNetworkState) {
        derivedStateOf {
            connectionNetworkState === NetworkConnectionState.Available
        }
    }
    AnimatedVisibility(
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + slideOutVertically(),
        visible = !isConnected
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp, end = 5.dp),
                color = DarkGray_1,
                strokeWidth = 2.dp
            )
            Text(
                text = "No internet connection...",
                fontFamily = Font(R.font.gilroy_semibold).toFontFamily(),
                fontSize = 14.sp,
                color = DarkGray_1
            )
        }
    }
    LaunchedEffect(connectionNetworkState){
        Log.d("isConnected", isConnected.toString())
    }
}
