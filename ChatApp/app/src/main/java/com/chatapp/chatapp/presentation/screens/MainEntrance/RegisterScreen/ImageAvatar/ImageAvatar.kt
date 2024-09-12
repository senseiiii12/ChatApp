package com.chatapp.chatapp.presentation.screens.MainEntrance.RegisterScreen.ImageAvatar

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import com.chatapp.chatapp.R
import com.chatapp.chatapp.ui.theme.Outline_1
import com.chatapp.chatapp.ui.theme.Surface_1
import com.chatapp.chatapp.ui.theme.Surface_2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
fun ImageAvatar(
    viewModel: ImageAvatarViewModel,
    imageUri: Uri?
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.setImageUri(uri)
            }
        }
    }

    imageUri?.let {
        Box {
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .padding(top = 5.dp, end = 5.dp)
                    .align(Alignment.TopEnd)
                    .border(2.dp, Surface_2, CircleShape)
                    .clip(CircleShape)
                    .background(Surface_1)
                    .size(16.dp)
                    .clickable { viewModel.clearImageUri() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier
                        .size(12.dp),
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Outline_1,
                )
            }
            Box(modifier = Modifier.zIndex(0f)) {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .border(2.dp, Surface_2, CircleShape)
                        .clip(CircleShape)
                        .size(80.dp)
                        .clickable {
                            val intent =
                                Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                )
                            launcher.launch(intent)
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    } ?: Box(
        modifier = Modifier
            .size(80.dp)
            .border(2.dp, Surface_2, CircleShape)
            .clip(CircleShape)
            .clickable {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                launcher.launch(intent)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Photo",
            fontFamily = FontFamily(Font(R.font.gilroy_medium)),
            fontSize = 10.sp,
            color = Surface_2,
        )
    }


}