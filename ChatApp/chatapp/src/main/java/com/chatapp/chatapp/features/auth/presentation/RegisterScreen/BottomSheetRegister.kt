package com.chatapp.chatapp.features.auth.presentation.RegisterScreen

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
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.buildpc.firstcompose.EnterScreen.components.ButtonEnter
import com.buildpc.firstcompose.EnterScreen.components.EditField
import com.chatapp.chatapp.R
import com.chatapp.chatapp.features.auth.presentation.RegisterScreen.ImageAvatar.ImageAvatar
import com.chatapp.chatapp.features.auth.presentation.RegisterScreen.ImageAvatar.ImageAvatarViewModel
import com.chatapp.chatapp.features.auth.presentation.Validator.ErrorMessage
import com.chatapp.chatapp.features.auth.presentation.Validator.ValidateViewModel
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.Success
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetRegister(
    viewModel: SignUpViewModel = hiltViewModel(),
    bottomSheetState: SheetState,
    snackbarHostState: SnackbarHostState,
    onSuccesRegistration: (Boolean) -> Unit
) {
    val validateViewModel: ValidateViewModel = viewModel()
    val viewModelImageAvatar: ImageAvatarViewModel = viewModel()
    val imageUri by viewModelImageAvatar.imageUri.collectAsState()
    val state = viewModel.singUpState.collectAsState(initial = null)

    var name = remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val stateRegisterValidate = validateViewModel.validationRegisterState.collectAsState()


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ImageAvatar(
            viewModel = viewModelImageAvatar,
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
                onValueChange = { name.value = it },
                value = name.value
            )
            ValidateCheck(
                modifier = Modifier.align(Alignment.CenterEnd),
                isSuccess = name.value.isNotEmpty()
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
                    email = it
                    validateViewModel.validateEmailRegister(email)
                },
                value = email
            )
            ValidateCheck(
                modifier = Modifier.align(Alignment.CenterEnd),
                isSuccess = stateRegisterValidate.value.errorEmailRegister.isEmpty() && email.isNotEmpty()
            )
        }
        ErrorMessage(state = stateRegisterValidate) { it.errorEmailRegister }
        Spacer(modifier = Modifier.height(20.dp))
        Box {
            EditField(
                placeholder = "Password",
                iconStart = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                visualTransformation = PasswordVisualTransformation(),
                onValueChange = {
                    password = it
                    validateViewModel.validatePasswordRegister(password)
                },
                value = password
            )
            ValidateCheck(
                modifier = Modifier.align(Alignment.CenterEnd),
                isSuccess = stateRegisterValidate.value.errorPasswordRegister.isEmpty() && password.isNotEmpty()
            )
        }
        ErrorMessage(state = stateRegisterValidate) { it.errorPasswordRegister }
        Spacer(modifier = Modifier.height(40.dp))
        ButtonEnter(
            text = "SignUp",
            OnClick = {
                if (stateRegisterValidate.value.errorEmailRegister.isEmpty() &&
                    stateRegisterValidate.value.errorPasswordRegister.isEmpty() &&
                    name.value.isNotEmpty()
                ) {
                    if (imageUri != null) {
                        viewModelImageAvatar.uploadImageToFirebase(imageUri!!) { downloadUrl ->
                            viewModel.registerUser(downloadUrl, name.value, email, password)
                        }
                    } else viewModel.registerUser(null, name.value, email, password)
                }
            },
        )
        Spacer(modifier = Modifier.height(50.dp))
    }




    LaunchedEffect(state.value?.isSuccess) {
        if (state.value?.isSuccess?.isNotEmpty() == true) {
            onSuccesRegistration(true)
            scope.launch {
                bottomSheetState.hide()
                snackbarHostState.showSnackbar(
                    message = state.value?.isSuccess ?: "",
                    actionLabel = "Dismiss",
                    duration = SnackbarDuration.Short
                )
            }.invokeOnCompletion {
                if (!bottomSheetState.isVisible) {
                    viewModel.hideSheet()
                }
            }
        }
    }
    LaunchedEffect(key1 = state.value?.isError) {
        scope.launch {
            if (state.value?.isError?.isNotEmpty() == true) {
                onSuccesRegistration(false)
                snackbarHostState.showSnackbar(
                    message = state.value?.isError ?: "",
                    actionLabel = "Dismiss",
                    duration = SnackbarDuration.Short
                )
            }
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