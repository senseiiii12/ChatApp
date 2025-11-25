package com.chatapp.chatapp.features.auth.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.features.auth.presentation.LoginScreen.LoginScreen
import com.chatapp.chatapp.features.auth.presentation.RegisterScreen.BottomSheetRegister
import com.chatapp.chatapp.features.auth.presentation.RegisterScreen.ImageAvatar.ImageAvatarViewModel
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.util.CustomSnackBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController,
    usersViewModel: UsersViewModel
) {
    val signInState by authViewModel.signInState.collectAsState()
    val signUpState by authViewModel.signUpState.collectAsState()
    val forgotPasswordState by authViewModel.forgotPasswordState.collectAsState()
    val showSignUpBottomSheet by authViewModel.showSignUpBottomSheet.collectAsState()

    val signUpBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val imageAvatarViewModel: ImageAvatarViewModel = viewModel()
    val imageUri by imageAvatarViewModel.imageUri.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var isSuccessRegistration by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = PrimaryBackground,
        snackbarHost = {
            CustomSnackBar(
                snackbarHostState = snackbarHostState,
                alignment = Alignment.BottomCenter,
                content = { snackBarData ->
                    Text(
                        text = snackBarData.visuals.message,
                        style = MyCustomTypography.SemiBold_14,
                        color = Color.White
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            LoginScreen(
                signInState = signInState,
                forgotPasswordState = forgotPasswordState,
                onEmailChange =  authViewModel::updateSignInEmail,
                onPasswordChange = authViewModel::updateSignInPassword,
                onSignInClick = { authViewModel.signInUser() },
                onShowRegister = { authViewModel.showSignUpBottomSheet() },
                onForgotPasswordEmailChange = authViewModel::updateForgotPasswordEmail,
                onSendPasswordReset = { authViewModel.forgotPassword() },
                onClearSignInError = { authViewModel.clearSignInError() },
                navController = navController,
                usersViewModel = usersViewModel
            )

            if (showSignUpBottomSheet) {
                ModalBottomSheet(
                    sheetState = signUpBottomSheetState,
                    onDismissRequest = {
                        authViewModel.hideSignUpBottomSheet()
                        imageAvatarViewModel.clearImageUri()
                    },
                    containerColor = PrimaryBackground,
                    scrimColor = Color.Black.copy(alpha = 0.8f)
                ) {
                    BottomSheetRegister(
                        signUpState = signUpState,
                        imageUri = imageUri,
                        imageAvatarViewModel = imageAvatarViewModel,
                        onNameChange = authViewModel::updateSignUpName,
                        onEmailChange = authViewModel::updateSignUpEmail,
                        onPasswordChange = authViewModel::updateSignUpPassword,
                        onSignUpClick = { context ->
                            authViewModel.signUpUser(context, imageUri)
                        },
                        onHideSheet = { authViewModel.hideSignUpBottomSheet() },
                        bottomSheetState = signUpBottomSheetState,
                        snackbarHostState = snackbarHostState,
                        onSuccessRegistration = { result ->
                            isSuccessRegistration = result
                        }
                    )
                }
            }
        }
    }
}