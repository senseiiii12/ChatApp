package com.chatapp.chatapp.features.auth.presentation.RegisterScreen.ImageAvatar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.rememberImagePainter
import com.chatapp.chatapp.ui.theme.MyCustomTypography
import com.chatapp.chatapp.ui.theme.Outline_1
import com.chatapp.chatapp.ui.theme.Surface_1

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
                    .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
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
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
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
            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            .clip(CircleShape)
            .clickable {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                launcher.launch(intent)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Photo",
            style = MyCustomTypography.Normal_10,
            color = Color.White.copy(alpha = 0.5f),
        )
    }
}