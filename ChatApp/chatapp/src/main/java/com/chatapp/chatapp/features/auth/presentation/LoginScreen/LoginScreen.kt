package com.chatapp.chatapp.features.auth.presentation.LoginScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.buildpc.firstcompose.EnterScreen.components.ButtonContinueWith
import com.buildpc.firstcompose.EnterScreen.components.ButtonEnter
import com.buildpc.firstcompose.EnterScreen.components.EditField
import com.buildpc.firstcompose.EnterScreen.components.SpacerOr
import com.chatapp.chatapp.R
import com.chatapp.chatapp.features.auth.presentation.Validator.ErrorMessage
import com.chatapp.chatapp.features.auth.presentation.Validator.ValidateViewModel
import com.chatapp.chatapp.features.navigation.Route
import com.chatapp.chatapp.core.presentation.UsersViewModel
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.ui.theme.PrimaryPurple
import com.chatapp.chatapp.ui.theme.Surface_1
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    navController: NavController,
    onClickShowRegister: () -> Unit,
    signInViewModel: SignInViewModel = hiltViewModel(),
    validateViewModel: ValidateViewModel = viewModel(),
    usersViewModel: UsersViewModel
) {
    val signInState by signInViewModel.signInState.collectAsState()
    val validationState = validateViewModel.validationLoginState.collectAsState()
    val currentUserId by usersViewModel.currentUserId.collectAsState()

    var isShownDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(email) {
        delay(300)
        validateViewModel.validateEmailLogin(email)
    }

    LaunchedEffect(signInState.isSuccess) {
        if (signInState.isSuccess) {
            currentUserId?.let { userId ->
                usersViewModel.updateUserOnlineStatus(userId, true)
                navigateToHomeScreen(navController)
            } ?: run {
                Log.e("LoginScreen", "Login successful but userId is null")
            }
        }
    }

    LaunchedEffect(signInState.errorMessage) {
        signInState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            signInViewModel.clearSignInError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PrimaryBackground
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryBackground)
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.8f, false),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.1f))

                Text(
                    text = "Login to ChatApp",
                    style = MyCustomTypography.SemiBold_24,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(40.dp))

                ButtonContinueWith(
                    text = "Continue with Google",
                    icon = R.drawable.icons8_google
                )

                Spacer(modifier = Modifier.height(20.dp))

                ButtonContinueWith(
                    text = "Continue with Apple",
                    icon = R.drawable.ri_apple_fill
                )

                Spacer(modifier = Modifier.height(20.dp))

                SpacerOr()

                Spacer(modifier = Modifier.height(20.dp))

                EditField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email",
                    iconStart = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                )

                ErrorMessage(state = validationState) { it.errorEmailLogin }

                Spacer(modifier = Modifier.height(20.dp))

                EditField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    iconStart = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPasswordField = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        modifier = Modifier.clickable {
                            isShownDialog = !isShownDialog
                        },
                        text = "Forgot Password?",
                        style = MyCustomTypography.Medium_14,
                        color = PrimaryPurple
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                ButtonEnter(
                    text = "Sign in",
                    isLoading = signInState.isLoading,
                    OnClick = {
                        if (validationState.value.errorEmailLogin.isEmpty() &&
                            email.isNotBlank() &&
                            password.isNotBlank()
                        ) {
                            signInViewModel.loginUser(email, password)
                        }
                    },
                    enabled = !signInState.isLoading &&
                            validationState.value.errorEmailLogin.isEmpty() &&
                            email.isNotBlank() &&
                            password.isNotBlank()
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Don't have account ?",
                    style = MyCustomTypography.Medium_14,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                ButtonEnter(
                    text = "Sign up",
                    enabled = !signInState.isLoading,
                    background = Surface_1,
                    textColor = PrimaryPurple,
                    borderColor = PrimaryPurple,
                    OnClick = onClickShowRegister
                )

                Spacer(modifier = Modifier.weight(0.1f))
            }

            Spacer(modifier = Modifier.width(20.dp))
        }
    }

    // Диалог сброса пароля
    if (isShownDialog) {
        DialogForgotPassword(
            onDismiss = { isShownDialog = !isShownDialog }
        )
    }
}

fun navigateToHomeScreen(navController: NavController) {
    navController.navigate(Route.HomePage.route) {
        popUpTo(navController.graph.startDestinationId) {
            inclusive = true
        }
        launchSingleTop = true
    }
}



