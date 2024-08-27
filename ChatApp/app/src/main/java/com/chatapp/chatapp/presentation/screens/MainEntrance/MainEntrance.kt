package com.chatapp.chatapp.presentation.screens.MainEntrance

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.presentation.screens.LoginScreen.LoginScreen
import com.chatapp.chatapp.presentation.screens.LoginScreen.SignInViewModel
import com.chatapp.chatapp.presentation.screens.RegisterScreen.BottomSheetRegister
import com.chatapp.chatapp.presentation.screens.RegisterScreen.ImageAvatar.ImageAvatarViewModel
import com.chatapp.chatapp.presentation.screens.RegisterScreen.SignUpViewModel
import com.chatapp.chatapp.ui.theme.PrimaryBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainEntrance(
    viewModelSignUp: SignUpViewModel = hiltViewModel(),
    navController: NavController
) {

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val viewModelImageAvatar: ImageAvatarViewModel = viewModel()
    val showBottomSheet by viewModelSignUp.showBottomSheet.collectAsState()

    LoginScreen(
        OnClickShowRegister = { viewModelSignUp.showSheet() },
        navController = navController
    )
    if (showBottomSheet) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = {
                viewModelSignUp.hideSheet()
                viewModelImageAvatar.clearImageUri()
            },
            containerColor = PrimaryBackground,
            scrimColor = Color.Black.copy(alpha = 0.8f)
        ) {
            BottomSheetRegister(bottomSheetState = bottomSheetState)
        }
    }
}