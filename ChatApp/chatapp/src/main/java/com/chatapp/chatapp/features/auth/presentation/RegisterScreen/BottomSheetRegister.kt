package com.chatapp.chatapp.features.auth.presentation.RegisterScreen

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buildpc.firstcompose.EnterScreen.components.ButtonEnter
import com.buildpc.firstcompose.EnterScreen.components.EditField
import com.chatapp.chatapp.features.auth.presentation.RegisterScreen.ImageAvatar.ImageAvatar
import com.chatapp.chatapp.features.auth.presentation.RegisterScreen.ImageAvatar.ImageAvatarViewModel
import com.chatapp.chatapp.features.auth.presentation.Validator.ErrorMessage
import com.chatapp.chatapp.features.auth.presentation.Validator.ValidateViewModel
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.Success
import com.chatapp.chatapp.util.CustomSnackBar
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetRegister(
    signUpState: SignUpState,
    imageUri: Uri?,
    imageAvatarViewModel: ImageAvatarViewModel,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignUpClick: (Context) -> Unit,
    onHideSheet: () -> Unit,
    bottomSheetState: SheetState,
    snackbarHostState: SnackbarHostState,
    onSuccessRegistration: (Boolean) -> Unit,
    validateViewModel: ValidateViewModel = viewModel()
) {
    val signUpValidationState = validateViewModel.validationSignUp.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageAvatar(
            viewModel = imageAvatarViewModel,
            imageUri = imageUri
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Register to ChatApp",
            style = MyCustomTypography.SemiBold_24,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box {
            EditField(
                placeholder = "Name",
                iconStart = Icons.Default.Person,
                keyboardType = KeyboardType.Text,
                visualTransformation = VisualTransformation.None,
                onValueChange = onNameChange,
                value = signUpState.name
            )
            ValidateCheck(
                modifier = Modifier.align(Alignment.CenterEnd),
                isSuccess = signUpState.name.isNotEmpty()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box {
            EditField(
                placeholder = "Email",
                iconStart = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
                visualTransformation = VisualTransformation.None,
                onValueChange = {
                    onEmailChange(it)
                    validateViewModel.validateEmailRegister(it)
                },
                value = signUpState.email
            )
            ValidateCheck(
                modifier = Modifier.align(Alignment.CenterEnd),
                isSuccess = signUpValidationState.value.errorEmailRegister.isEmpty()
                        && signUpState.email.isNotEmpty()
            )
        }
        ErrorMessage(state = signUpValidationState) { it.errorEmailRegister }

        Spacer(modifier = Modifier.height(20.dp))

        Box {
            EditField(
                placeholder = "Password",
                iconStart = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = {
                    onPasswordChange(it)
                    validateViewModel.validatePasswordRegister(it)
                },
                value = signUpState.password
            )
            ValidateCheck(
                modifier = Modifier.align(Alignment.CenterEnd),
                isSuccess = signUpValidationState.value.errorPasswordRegister.isEmpty()
                        && signUpState.password.isNotEmpty()
            )
        }
        ErrorMessage(state = signUpValidationState) { it.errorPasswordRegister }

        Spacer(modifier = Modifier.height(40.dp))

        ButtonEnter(
            text = "SignUp",
            isLoading = signUpState.isLoading,
            enabled = signUpValidationState.value.validationSuccess && signUpState.allFieldsNotEmpty,
            OnClick = {
                if (signUpValidationState.value.validationSuccess && signUpState.allFieldsNotEmpty) {
                    onSignUpClick(context)
                }
            },
        )
        Spacer(modifier = Modifier.height(50.dp))
    }




    LaunchedEffect(signUpState.isSuccess) {
        if (signUpState.isSuccess) {
            onSuccessRegistration(true)
            scope.launch {
                bottomSheetState.hide()
                snackbarHostState.showSnackbar(
                    message = "Sign Up success",
                    actionLabel = "Dismiss",
                    duration = SnackbarDuration.Short
                )
            }.invokeOnCompletion {
                if (!bottomSheetState.isVisible) {
                    onHideSheet()
                }
            }
        }
    }

    LaunchedEffect(key1 = signUpState.errorMessage) {
        if (signUpState.errorMessage.isNotEmpty()) {
            Toast.makeText(context,signUpState.errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun ValidateCheck(
    modifier: Modifier,
    isSuccess: Boolean
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = isSuccess,
        enter = slideInVertically(initialOffsetY = { -it }) + expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
    ) {
        Icon(
            modifier = Modifier
                .padding(end = 10.dp),
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Success
        )
    }
}