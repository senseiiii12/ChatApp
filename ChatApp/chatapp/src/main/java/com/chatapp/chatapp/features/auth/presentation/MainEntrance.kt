package com.chatapp.chatapp.features.auth.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.features.auth.presentation.LoginScreen.LoginScreen
import com.chatapp.chatapp.features.auth.presentation.RegisterScreen.BottomSheetRegister
import com.chatapp.chatapp.features.auth.presentation.RegisterScreen.ImageAvatar.ImageAvatarViewModel
import com.chatapp.chatapp.features.auth.presentation.RegisterScreen.SignUpViewModel
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.util.CustomSnackBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainEntrance(
    viewModelSignUp: SignUpViewModel = hiltViewModel(),
    navController: NavController
) {

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val viewModelImageAvatar: ImageAvatarViewModel = viewModel()
    val showBottomSheet by viewModelSignUp.showBottomSheet.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isSuccessRegistration by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = PrimaryBackground,
        snackbarHost = {
            CustomSnackBar(
                snackbarHostState = snackbarHostState,
                isSuccess = isSuccessRegistration
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
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
                    BottomSheetRegister(
                        bottomSheetState = bottomSheetState,
                        snackbarHostState = snackbarHostState,
                        onSuccesRegistration = { result ->
                            isSuccessRegistration = result
                        }
                    )
                }
            }
        }
    }
}