package com.chatapp.chatapp.util.CustomSnackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay


private const val DISMISS_ANIMATION_DELAY = 250L


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSnackbarHost(
    controller: SnackbarController,
    modifier: Modifier = Modifier
) {
    val snackBarEvents by controller.snackBarEvents.collectAsState()
    var visible by remember { mutableStateOf(false) }
    var data by remember { mutableStateOf<SnackbarData?>(null) }

    LaunchedEffect(snackBarEvents) {
        when (val event = snackBarEvents) {
            is SnackBarState.Open -> {
                data = event.data
                visible = true
                delay(event.data.durationMillis)
                controller.close()
            }
            is SnackBarState.Close -> {
                visible = false
                delay(DISMISS_ANIMATION_DELAY)
                controller.notifyHidden()
            }
            null -> {}
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(animationSpec = spring(),initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier.fillMaxWidth()
    ) {
        data?.let { data ->
            val dismissState = rememberDismissState(
                confirmValueChange = {
                    controller.close()
                    true
                }
            )
            SwipeToDismiss(
                state = dismissState,
                background = {},
                dismissContent = {
                    Row(
                        modifier = Modifier.padding(data.outerPadding).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier
                                .shadow(data.elevation, data.shape)
                                .background(data.backgroundColor, data.shape)
                                .padding(data.innerPadding)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                data.icon?.invoke()
                                Spacer(modifier = Modifier.width(8.dp))
                                data.text.invoke()

                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Spacer(modifier = Modifier.width(16.dp))
                                data.customAction?.invoke()
                                data.dismissAction?.invoke()
                            }
                        }
                    }
                }
            )
        }
    }
}


