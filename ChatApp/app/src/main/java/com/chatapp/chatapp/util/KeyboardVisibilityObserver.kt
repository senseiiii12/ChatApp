package com.chatapp.chatapp.util

import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun KeyboardVisibilityObserver(onKeyboardVisibilityChanged: (Boolean) -> Unit) {
    val view = LocalView.current
    val density = LocalDensity.current.density

    val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            val rect = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.height
            val keypadHeight = screenHeight - rect.bottom
            onKeyboardVisibilityChanged(keypadHeight > screenHeight * 0.15)
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main) {
            view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        }
    }

    // Clean up the listener when the composable is disposed
    DisposableEffect(Unit) {
        view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        onDispose { }
    }
}
