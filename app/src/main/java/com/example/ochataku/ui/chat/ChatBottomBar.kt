package com.example.ochataku.ui.chat

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.ochataku.R
import com.example.ochataku.model.MessageDisplay
import com.example.ochataku.utils.PermissionUtils
import com.example.ochataku.viewmodel.ChatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

// ---- Bottom Bar ----
@Composable
fun ChatInputBar(
    context: Context,
    messageText: TextFieldValue,
    onMessageTextChange: (TextFieldValue) -> Unit,
    coroutineScope: CoroutineScope,
    listState: LazyListState,
    messages: List<MessageDisplay>,
    viewModel: ChatViewModel,
    currentUserId: Long,
    peerId: Long,
    convId: Long,
    isGroup: Boolean,
    mediaPickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    imageCaptureLauncher: ManagedActivityResultLauncher<Void?, Bitmap?>,
    videoCaptureLauncher: ManagedActivityResultLauncher<Uri, Boolean>,
    selectedMediaUri: Uri?,
    onMediaUriSelected: (Uri?) -> Unit
) {
    var isVoiceMode by remember { mutableStateOf(false) }
    var isDialogOpen by remember { mutableStateOf(false) }
    val quoted by viewModel.quotedMessage.collectAsState()


    Surface(
        modifier = Modifier.imePadding(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ✅ 显示引用消息
            quoted?.let { msg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.replying_to),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            when (msg.type) {
                                "text" -> Text(msg.preview, maxLines = 1)
                                "image" -> Text(
                                    "[图片]",
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                "video" -> Text(
                                    "[视频]",
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                "voice" -> Text(
                                    "[语音消息]",
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                else -> Text(
                                    "[未知消息]",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.cancelQuote() }) {
                            Icon(Icons.Default.Close, contentDescription = "取消引用")
                        }
                    }
                }
            }

            Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 切换按钮
            IconButton(onClick = { isVoiceMode = !isVoiceMode }) {
                Icon(
                    imageVector = if (isVoiceMode) Icons.Default.Keyboard else Icons.Default.Mic,
                    contentDescription = null
                )
            }

            Spacer(Modifier.width(4.dp))

            // 语音或文本输入
            if (isVoiceMode) {
                VoiceRecordButton(
                    context = context,
                    modifier = Modifier.weight(1f),
                    viewModel = viewModel,
                    currentUserId = currentUserId,
                    peerId = peerId,
                    convId = convId,
                    isGroup = isGroup
                )
            } else {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.enter_something),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }

            Spacer(Modifier.width(8.dp))

            IconButton(onClick = { isDialogOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }

            Spacer(Modifier.width(4.dp))

            if (!isVoiceMode && messageText.text.isNotBlank()) {
                Button(onClick = {
                    viewModel.sendMessage(
                        context = context,
                        mediaUri = null,
                        messageType = "text",
                        content = messageText.text,
                        senderId = currentUserId,
                        receiverId = peerId,
                        convId = convId,
                        isGroup = isGroup
                    ) { success ->
                        if (success) {
                            viewModel.loadMessagesByConvId(context, convId)
                            onMessageTextChange(TextFieldValue())
                            coroutineScope.launch {
                                delay(100)
                                listState.animateScrollToItem(messages.lastIndex)
                            }
                        }
                    }
                }) {
                    Text(stringResource(R.string.send))
                }
            }
        }
    }

    // 弹出功能选择对话框
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            title = { Text(stringResource(R.string.select_function)) },
            confirmButton = {},
            text = {
                Column {
                    val activity = context as? Activity
                    TextButton(onClick = {
                        isDialogOpen = false
                        activity?.let {
                            mediaPickerLauncher.launch("*/*")
                        }
                    }) { Text(stringResource(R.string.select_from_photograph)) }

                    TextButton(onClick = {
                        isDialogOpen = false
                        activity?.let {
                            PermissionUtils.requestCameraPermission(it) {
                                imageCaptureLauncher.launch(null)
                            }
                        }
                    }) { Text(stringResource(R.string.take_photo)) }

                    TextButton(onClick = {
                        isDialogOpen = false
                        activity?.let {
                            PermissionUtils.requestCameraPermission(it) {
                                val vf = File(
                                    context.cacheDir,
                                    "recorded_video_${System.currentTimeMillis()}.mp4"
                                )
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    vf
                                )
                                onMediaUriSelected(uri)
                                videoCaptureLauncher.launch(uri)
                            }
                        }
                    }) { Text(stringResource(R.string.record_video)) }
                }
            }
        )
    }
    }
}
