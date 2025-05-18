package com.example.ochataku.ui.chat

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.ochataku.R

@Composable
fun MediaEditDialog(
    showDialog: Boolean,
    selectedMediaUri: Uri?,
    onDismiss: () -> Unit,
    onCrop: (Uri) -> Unit,
    onRetake: () -> Unit,
    onReselect: () -> Unit,
    onSend: () -> Unit
) {
    if (showDialog && selectedMediaUri != null) {
        val context = LocalContext.current
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(selectedMediaUri)


        Dialog(onDismissRequest = onDismiss) {
            Surface(
                color = Color.Black,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val isVideo = mimeType?.contains("video") == true
                    val isImage = selectedMediaUri.toString().contains("image")

                    when {
                        isImage -> {
                            AsyncImage(
                                model = selectedMediaUri,
                                contentDescription = stringResource(R.string.image_preview),
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        isVideo -> {
                            VideoPlayer(uri = selectedMediaUri)
                        }
                        else -> {
                            Text(stringResource(R.string.cannot_preview_media), color = Color.White)
                        }
                    }


                    Spacer(modifier = Modifier.height(24.dp))

                    // 操作按钮组
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { onCrop(selectedMediaUri) }) {
                            Text(stringResource(R.string.crop), color = Color.White)
                        }
                        TextButton(onClick = onRetake) {
                            Text(stringResource(R.string.retake), color = Color.White)
                        }
                        TextButton(onClick = onReselect) {
                            Text(stringResource(R.string.reselect), color = Color.White)
                        }
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel), color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onSend,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.send), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayer(uri: Uri) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}

