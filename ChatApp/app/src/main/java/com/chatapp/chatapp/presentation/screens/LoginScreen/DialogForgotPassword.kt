package com.chatapp.chatapp.presentation.screens.LoginScreen

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.buildpc.firstcompose.EnterScreen.components.ButtonEnter
import com.buildpc.firstcompose.EnterScreen.components.EditField
import com.chatapp.chatapp.R
import com.chatapp.chatapp.presentation.ValidateViewModel
import com.chatapp.chatapp.ui.theme.PrimaryBackground
import com.chatapp.chatapp.util.ErrorMessage
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun DialogForgotPassword(
    viewModel: SignInViewModel = viewModel(),
    validateViewModel: ValidateViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    var forgotEmail by rememberSaveable { mutableStateOf("") }
    var submit by rememberSaveable { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryBackground)
        ) {
            AnimatedContent(targetState = submit) { isSubmit ->
                if (isSubmit) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "An email",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontFamily = FontFamily(Font(R.font.gilroy_medium))
                        )
                        Text(
                            text = forgotEmail,
                            fontSize = 14.sp,
                            color = Color.White,
                            fontFamily = FontFamily(Font(R.font.gilroy_bold))
                        )
                        Text(
                            text = "has been sent to reset your password",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontFamily = FontFamily(Font(R.font.gilroy_medium))
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
                            modifier = Modifier.padding(start = 2.dp, bottom = 2.dp),
                            text = "Enter the account's email address",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontFamily = FontFamily(Font(R.font.gilroy_medium))
                        )
                        EditField(
                            placeholder = "Email",
                            iconStart = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            visualTransformation = VisualTransformation.None,
                            onValueChange = {
                                forgotEmail = it
                                validateViewModel.validateForgotEmail(forgotEmail)
                            },
                            value = forgotEmail
                        )
                        ErrorMessage(
                            modifier = Modifier.align(Alignment.Start),
                            message = validateViewModel.errorForgotEmail.value
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ButtonEnter(
                            text = "Reset password",
                            OnClick = {
                                if (validateViewModel.errorForgotEmail.value.isEmpty() && forgotEmail.isNotEmpty()){
                                    viewModel.forgotPassword(forgotEmail)
                                    submit = !submit
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