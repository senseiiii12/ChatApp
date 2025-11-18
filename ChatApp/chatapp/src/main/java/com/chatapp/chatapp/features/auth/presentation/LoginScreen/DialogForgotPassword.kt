package com.chatapp.chatapp.features.auth.presentation.LoginScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.buildpc.firstcompose.EnterScreen.components.ButtonEnter
import com.buildpc.firstcompose.EnterScreen.components.EditField
import com.chatapp.chatapp.features.auth.presentation.Validator.ValidateViewModel
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.features.auth.presentation.Validator.ErrorMessage
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chatapp.chatapp.features.auth.presentation.ForgotPasswordState
import com.chatapp.chatapp.ui.theme.DarkGray_2
import com.chatapp.chatapp.ui.theme.MyCustomTypography


@Composable
fun DialogForgotPassword(
    forgotPasswordState: ForgotPasswordState,
    onEmailChange: (String) -> Unit,
    onSendPasswordReset: () -> Unit,
    onDismiss: () -> Unit,
    validateViewModel: ValidateViewModel = viewModel()
) {
    val forgotPasswordValidationState = validateViewModel.validationForgotPassword.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryBackground)
        ) {
            AnimatedContent(targetState = forgotPasswordState.isSuccess) { isSuccess ->
                if (isSuccess) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "An email",
                            style = MyCustomTypography.Normal_12,
                            color = Color.White.copy(alpha = 0.5f),
                        )
                        Text(
                            text = forgotPasswordState.email,
                            style = MyCustomTypography.Bold_14,
                            color = Color.White
                        )
                        Text(
                            text = "has been sent to reset your password",
                            style = MyCustomTypography.Normal_12,
                            color = Color.White.copy(alpha = 0.5f),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ButtonEnter(
                            text = "Ok",
                            OnClick = onDismiss
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                } else {
                    Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            modifier = Modifier.padding(start = 2.dp, bottom = 16.dp),
                            text = "Enter the account's email address",
                            style = MyCustomTypography.Normal_12,
                            color = Color.White
                        )
                        EditField(
                            placeholder = "Email",
                            iconStart = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            visualTransformation = VisualTransformation.None,
                            onValueChange = {
                                onEmailChange(it)
                                validateViewModel.validateForgotEmail(it)
                            },
                            value = forgotPasswordState.email
                        )
                        ErrorMessage(state = forgotPasswordValidationState) { it.errorForgotEmail }
                        Spacer(modifier = Modifier.height(16.dp))
                        ButtonEnter(
                            background = DarkGray_2,
                            text = "Reset password",
                            isLoading = forgotPasswordState.isLoading,
                            OnClick = {
                                if (forgotPasswordValidationState.value.validationSuccess
                                    && forgotPasswordState.email.isNotEmpty()
                                ) {
                                    onSendPasswordReset()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}