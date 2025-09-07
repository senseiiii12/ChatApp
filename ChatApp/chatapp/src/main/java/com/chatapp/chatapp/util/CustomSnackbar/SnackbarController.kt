package com.chatapp.chatapp.util.CustomSnackbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class SnackBarState {
    data class Open(val data: SnackbarData) : SnackBarState()
    object Close : SnackBarState()
}

class SnackbarController(private val scope: CoroutineScope) {
    private val _snackBarEvents = MutableStateFlow<SnackBarState?>(null)
    val snackBarEvents: StateFlow<SnackBarState?> = _snackBarEvents.asStateFlow()

    fun show(data: SnackbarData) {
        if (_snackBarEvents.value != null) return
        _snackBarEvents.value = SnackBarState.Open(data)
    }

    fun close() {
        _snackBarEvents.value = SnackBarState.Close
    }

    fun notifyHidden() {
        _snackBarEvents.value = null
    }
}

data class SnackbarData(
    val icon: (@Composable (() -> Unit))? = null,
    val text: @Composable () -> Unit,
    val customAction: (@Composable (() -> Unit))? = null,
    val dismissAction: (@Composable (() -> Unit))? = null,
    val backgroundColor: Color = Color.DarkGray,
    val shape: Shape = RoundedCornerShape(12.dp),
    val elevation: Dp = 6.dp,
    val innerPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    val outerPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    val durationMillis: Long = 3000
)
