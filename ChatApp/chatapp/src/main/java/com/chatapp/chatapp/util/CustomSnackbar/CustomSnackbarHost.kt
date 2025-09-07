package com.chatapp.chatapp.util.CustomSnackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalDragOrCancellation
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.awaitVerticalDragOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs


private const val DISMISS_ANIMATION_DELAY = 200


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSnackbarHost(
    controller: SnackbarController,
    modifier: Modifier = Modifier
) {
    val snackBarEvents by controller.snackBarEvents.collectAsState()
    var animationVisible by remember { mutableStateOf(false) }
    var snackBarData by remember { mutableStateOf<SnackbarData?>(null) }
    var isDismissingBySwipe by remember { mutableStateOf(false) }

    LaunchedEffect(snackBarEvents) {
        when (val event = snackBarEvents) {
            is SnackBarState.Open -> {
                snackBarData = event.data
                animationVisible = true
                isDismissingBySwipe = false
                delay(event.data.durationMillis)
                if (!isDismissingBySwipe) {
                    controller.close()
                }
            }
            is SnackBarState.Close -> {
                animationVisible = false
                if (isDismissingBySwipe){
                    controller.notifyHidden()
                }else{
                    delay(DISMISS_ANIMATION_DELAY.toLong())
                    controller.notifyHidden()
                }
            }
            null -> {}
        }
    }
    val exitAnim: ExitTransition =
        if (!isDismissingBySwipe) slideOutVertically(animationSpec = spring(), targetOffsetY = { -it }) + fadeOut()
        else ExitTransition.None

    AnimatedVisibility(
        visible = animationVisible,
        enter = slideInVertically(animationSpec = spring(), initialOffsetY = { -it }) + fadeIn(),
        exit = exitAnim,
        modifier = modifier.fillMaxWidth()
    ) {
        snackBarData?.let { snackbarData ->
            val scope = rememberCoroutineScope()
            val offsetX = remember { Animatable(0f) }
            val offsetY = remember { Animatable(0f) }
            var size by remember { mutableStateOf(IntSize.Zero) }
            val density = LocalDensity.current

            val thresholdX = (size.width * 0.05).toFloat()
            val thresholdY = (size.height * 0.05).toFloat()

            val paddingHorizontalPx = with(density) {
                snackbarData.outerPadding.calculateLeftPadding(LayoutDirection.Ltr).toPx()
            }
            val paddingVerticalPx = with(density) {
                snackbarData.outerPadding.calculateTopPadding().toPx()
            }

            Row(
                modifier = Modifier
                    .padding(snackbarData.outerPadding)
                    .fillMaxWidth()
                    .onSizeChanged { size = it }
                    .offset {
                        IntOffset(
                            offsetX.value.toInt(),
                            offsetY.value.toInt()
                        )
                    }
                    .pointerInput(Unit) {
                        coroutineScope {
                            while (true) {
                                awaitPointerEventScope {
                                    val down = awaitFirstDown()

                                    var overSlop = Offset.Zero
                                    val drag = awaitTouchSlopOrCancellation(down.id) {
                                        change: PointerInputChange, over: Offset ->
                                            overSlop = over
                                            change.consume()
                                        }

                                    if (drag != null) {
                                        val isVertical = abs(overSlop.y) > abs(overSlop.x)
                                        if (isVertical && overSlop.y >= 0) {
                                            return@awaitPointerEventScope
                                        }

                                        scope.launch {
                                            if (isVertical) {
                                                val deltaY = if (overSlop.y > 0) 0f else overSlop.y
                                                offsetY.snapTo(offsetY.value + deltaY)
                                            } else {
                                                offsetX.snapTo(offsetX.value + overSlop.x)
                                            }
                                        }
                                        drag.consume()

                                        var canceled = false

                                        while (!canceled) {
                                            val next =
                                                if (isVertical) awaitVerticalDragOrCancellation(drag.id)
                                                else awaitHorizontalDragOrCancellation(drag.id)
                                            if (next == null) {
                                                canceled = true
                                            } else {
                                                val deltaNext =
                                                    if (isVertical) next.positionChange().y
                                                    else next.positionChange().x
                                                val newDelta =
                                                    if (isVertical && deltaNext > 0) 0f
                                                    else deltaNext
                                                scope.launch {
                                                    if (isVertical) offsetY.snapTo(offsetY.value + newDelta)
                                                    else offsetX.snapTo(offsetX.value + newDelta)
                                                }
                                                next.consume()
                                            }
                                        }

                                        val absX = abs(offsetX.value)
                                        val absY = abs(offsetY.value)
                                        val dismissDirection = when {
                                            absY > thresholdY && offsetY.value < 0 -> "up"
                                            absX > thresholdX -> if (offsetX.value > 0) "right" else "left"
                                            else -> null
                                        }
                                        if (dismissDirection != null) {
                                            val targetX = when (dismissDirection) {
                                                "left" -> -(size.width.toFloat() + paddingHorizontalPx * 2)
                                                "right" -> size.width.toFloat() + paddingHorizontalPx * 2
                                                else -> 0f
                                            }
                                            val targetY =
                                                if (dismissDirection == "up")-(size.height.toFloat() + paddingVerticalPx * 2)
                                                else 0f

                                            isDismissingBySwipe = true

                                            scope.launch {
                                                coroutineScope {
                                                    launch {
                                                        offsetX.animateTo(targetX, animationSpec = spring())
                                                    }
                                                    launch {
                                                        offsetY.animateTo(targetY, animationSpec = spring())
                                                    }
                                                }
                                                controller.close()
                                            }
                                        } else {
                                            scope.launch {
                                                offsetX.animateTo(0f, animationSpec = spring())
                                            }
                                            scope.launch {
                                                offsetY.animateTo(0f, animationSpec = spring())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier
                        .shadow(snackbarData.elevation, snackbarData.shape)
                        .background(snackbarData.backgroundColor, snackbarData.shape)
                        .padding(snackbarData.innerPadding)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        snackbarData.icon?.invoke()
                        Spacer(modifier = Modifier.width(8.dp))
                        snackbarData.text.invoke()
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.width(16.dp))
                        snackbarData.customAction?.invoke()
                        snackbarData.dismissAction?.invoke()
                    }
                }
            }
        }
    }
}


