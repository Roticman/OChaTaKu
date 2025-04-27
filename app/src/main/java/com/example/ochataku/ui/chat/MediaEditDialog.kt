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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage

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
                    // 图片预览
                    AsyncImage(
                        model = selectedMediaUri,
                        contentDescription = "预览",
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 操作按钮组
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { onCrop(selectedMediaUri) }) {
                            Text("裁剪", color = Color.White)
                        }
                        TextButton(onClick = onRetake) {
                            Text("重拍", color = Color.White)
                        }
                        TextButton(onClick = onReselect) {
                            Text("重选", color = Color.White)
                        }
                        TextButton(onClick = onDismiss) {
                            Text("取消", color = Color.White)
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
                        Text("发送", color = Color.White)
                    }
                }
            }
        }
    }
}
