package com.chatapp.chatapp.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    action: @Composable ((SnackbarData) -> Unit)? = null,
    dismissAction: @Composable ((SnackbarData) -> Unit)? = null,
    content: @Composable (SnackbarData) -> Unit,
    alignment: Alignment = Alignment.TopCenter,
    containerColor: Color = DarkGray_2,
    shape: Shape = RoundedCornerShape(18.dp),
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(
                modifier = modifier.padding(10.dp),
                shape = shape,
                containerColor = containerColor,
                action = action?.let { { it(data) } },
                dismissAction = dismissAction?.let { { it(data) } },
                content = { content(data) }
            )
        }
    }
}