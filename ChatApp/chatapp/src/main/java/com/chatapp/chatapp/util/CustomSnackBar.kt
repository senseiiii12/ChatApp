package com.chatapp.chatapp.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.DarkGray_1
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.Error
import com.chatapp.chatapp.ui.theme.Online
import com.chatapp.chatapp.ui.theme.Outline_1

@Composable
fun CustomSnackBar(
    snackbarHostState: SnackbarHostState,
    containerColor: Color = DarkGray_2,
    isSuccess: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
                modifier = Modifier.padding(10.dp),
                shape = RoundedCornerShape(18.dp),
                containerColor = containerColor,
                contentColor = if (isSuccess) Online else Error,
                actionContentColor = DarkGray_1,
                action = {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        onClick = { data.dismiss() }
                    ) {
                        Text(
                            text = data.visuals.actionLabel!!,
                            fontSize = 12.sp,
                            fontFamily = Font(R.font.gilroy_medium).toFontFamily(),
                            color = Outline_1
                        )
                    }
                }
            ) {
                Text(
                    text = data.visuals.message,
                    fontSize = 14.sp,
                    fontFamily = Font(R.font.gilroy_medium).toFontFamily(),
                )
            }
        }
    }
}