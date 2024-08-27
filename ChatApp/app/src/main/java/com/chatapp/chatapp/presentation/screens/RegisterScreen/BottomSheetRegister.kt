package com.chatapp.chatapp.presentation.screens.RegisterScreen

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
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.chatapp.chatapp.presentation.ValidateViewModel
import com.chatapp.chatapp.presentation.screens.RegisterScreen.ImageAvatar.ImageAvatar
import com.chatapp.chatapp.presentation.screens.RegisterScreen.ImageAvatar.ImageAvatarViewModel
import com.chatapp.chatapp.ui.theme.Success
import com.chatapp.chatapp.util.ErrorMessage
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetRegister(
    viewModel: SignUpViewModel = hiltViewModel(),
    validateViewModel: ValidateViewModel = viewModel(),
    bottomSheetState: SheetState
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val viewModelImageAvatar: ImageAvatarViewModel = viewModel()
    val imageUri by viewModelImageAvatar.imageUri.collectAsState()
    val base64String by viewModelImageAvatar.base64String.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val state = viewModel.singUpState.collectAsState(initial = null)

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
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Register to ChatApp",
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            fontSize = 24.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box{
            EditField(
                placeholder = "Name",
                iconStart = Icons.Default.Person,
                keyboardType = KeyboardType.Text,
                visualTransformation = VisualTransformation.None,
                onValueChange = { name = it },
                value = name
            )
            ValidateCheck(
                modifier = Modifier.align(Alignment.CenterEnd),
                isSuccess = name.isNotEmpty()
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
                isSuccess = validateViewModel.errorEmailRegister.value.isEmpty() && email.isNotEmpty()
            )
        }
        ErrorMessage(
            modifier = Modifier.align(Alignment.Start),
            message = validateViewModel.errorEmailRegister.value
        )
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
                isSuccess = validateViewModel.errorPasswordRegister.value.isEmpty() && password.isNotEmpty()
            )
        }
        ErrorMessage(
            modifier = Modifier.align(Alignment.Start),
            message = validateViewModel.errorPasswordRegister.value
        )
        Spacer(modifier = Modifier.height(40.dp))
        ButtonEnter(
            text = "SignUp",
            OnClick = {
                viewModel.registerUser(name,email, password)
            },
        )
        Spacer(modifier = Modifier.height(50.dp))


    }


    LaunchedEffect(state.value?.isSuccess) {
        if (state.value?.isSuccess?.isNotEmpty() == true) {
            Toasty.success(context, "Success SignUp!", Toast.LENGTH_SHORT, true).show()
            scope.launch {
                bottomSheetState.hide()
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
                val error = state.value?.isError
                Toast.makeText(context, "${error}", Toast.LENGTH_SHORT).show()
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