package com.example.ochataku.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ochataku.R

@Composable
fun FullScreenMediaDialog(
    mediaUrl: String,
    mediaType: String, // "image" or "video"
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            when (mediaType) {
                "image" -> {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onDismiss() },
                        contentScale = ContentScale.Fit
                    )
                }

                "video" -> {
                    AndroidView(
                        factory = {
                            android.widget.VideoView(it).apply {
                                setVideoPath(mediaUrl)
                                setOnPreparedListener { mp ->
                                    mp.start()
                                    mp.isLooping = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onDismiss() }
                    )
                }
            }

            IconButton(
                onClick = { onSaveClick() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(R.string.save),
                    tint = Color.White
                )
            }
        }
    }
}
