package com.chatapp.chatapp.features.auth.presentation.LoginScreen

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    OnClickShowRegister: () -> Unit,
    signInViewModel: SignInViewModel = hiltViewModel(),
    validateViewModel: ValidateViewModel = viewModel(),
    usersViewModel: UsersViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var isShownDialog by rememberSaveable { mutableStateOf(false) }
    val stateLoginValidate = validateViewModel.validationLoginState.collectAsState()
    val stateLogin = signInViewModel.signInState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(email) {
        delay(300)
        validateViewModel.validateEmailLogin(email)
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryBackground)
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
            key("Email") {
                EditField(
                    placeholder = "Email",
                    iconStart = Icons.Default.Email,
                    visualTransformation = VisualTransformation.None,
                    keyboardType = KeyboardType.Email,
                    onValueChange = { email = it },
                    value = email
                )
            }
            ErrorMessage(state = stateLoginValidate) { it.errorEmailLogin }
            Spacer(modifier = Modifier.height(20.dp))
            key("Password") {
                EditField(
                    placeholder = "Password",
                    iconStart = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardType = KeyboardType.Password,
                    onValueChange = { password = it },
                    value = password
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    modifier = Modifier
                        .clickable {
                            isShownDialog = !isShownDialog
                    },
                    text = "Forgot Password?",
                    style = MyCustomTypography.Medium_14,
                    color = PrimaryPurple
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            val onSignInClick = remember {
                {
                    if (stateLoginValidate.value.errorEmailLogin.isEmpty()) {
                        scope.launch {
                            signInViewModel.loginUser(email, password) {
                                signInViewModel.getCurrentUserUID()?.let {
                                    usersViewModel.updateUserOnlineStatus(it, true)
                                }
                            }
                        }
                    }
                }
            }
            ButtonEnter(
                text = "Sign in",
                OnClick = onSignInClick
            )
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Don’t have account ?",
                style = MyCustomTypography.Medium_14,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))
            ButtonEnter(
                text = "Sign up",
                background = Surface_1,
                textColor = PrimaryPurple,
                borderColor = PrimaryPurple,
                OnClick = OnClickShowRegister
            )
            Spacer(modifier = Modifier.weight(0.1f))
        }
        Spacer(modifier = Modifier.width(20.dp))
    }

    if (isShownDialog) {
        DialogForgotPassword(
            onDismiss = { isShownDialog = !isShownDialog }
        )
    }


    LaunchedEffect(key1 = stateLogin.value.isSuccess) {
        scope.launch {
            if (stateLogin.value.isSuccess?.isNotEmpty() == true) {
                val success = stateLogin.value.isSuccess
                navigateToHomeScreen(navController)
            }
        }
    }
    LaunchedEffect(key1 = stateLogin.value.isError) {
        scope.launch {
            if (stateLogin.value.isError?.isNotEmpty() == true) {
                val error = stateLogin.value.isError

            }
        }
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



