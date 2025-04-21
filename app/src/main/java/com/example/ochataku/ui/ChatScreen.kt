package com.example.ochataku.ui

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ochataku.service.ApiClient.BASE_URL
import com.example.ochataku.service.ApiClient.connectAndListen
import com.example.ochataku.service.MessageDisplay
import com.example.ochataku.viewmodel.ChatViewModel
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date


// MediaRecorder 和录音文件路径
var mediaRecorder: MediaRecorder? = null
var audioFilePath: String? = null

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    convId: Long,
    peerId: Long,
    peerName: String,
    isGroup: Boolean,
    currentUserId: Long,
    peerAvatarUrl: String?,
    currentUserAvatarUrl: String?
) {
    val viewModel: ChatViewModel = hiltViewModel()
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf(TextFieldValue()) }

    // 屏幕宽度，用于气泡最大宽度
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxBubbleWidth = screenWidth * 0.7f
    val minBubbleWidth = 64.dp


    // 加载消息（网络优先，失败回退本地）
    LaunchedEffect(convId) {
        viewModel.loadMessagesByConvId(context, convId)
        connectAndListen(convId) { messageJson ->
            val message = parseMessage(messageJson)
            viewModel.addMessage(message) // 添加到 UI 列表中
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(peerName) },
                actions = {
                    IconButton(onClick = {
                        val route = if (isGroup) "groupDetail/$convId" else "chatDetail/$convId"
                        navController.navigate(route)
                    }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                }
            )
        },
        bottomBar = {
            var isVoiceMode by remember { mutableStateOf(false) }
            var isRecording by remember { mutableStateOf(false) }
            var isDialogOpen by remember { mutableStateOf(false) }
            Surface(
                color = MaterialTheme.colorScheme.background, // 显式设置底部栏背景
                tonalElevation = 3.dp
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 语音/键盘切换按钮
                    IconButton(onClick = { isVoiceMode = !isVoiceMode }) {
                        Icon(
                            imageVector = if (isVoiceMode) Icons.Default.Keyboard else Icons.Default.Mic,
                            contentDescription = null
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    if (isVoiceMode) {
                        // 语音录制按钮（长按）
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            isRecording = true
                                            startRecording(context)
                                            Toast.makeText(
                                                context,
                                                "开始录音...",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        },
                                        onPress = {
                                            tryAwaitRelease()
//                                        isRecording = true
                                            isRecording = false
                                            stopRecording { audioPath ->
                                                Toast.makeText(
                                                    context,
                                                    "录音发送：$audioPath",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // 🔽 这里发消息
                                                viewModel.sendMessage(
                                                    context = context,
                                                    mediaUri = Uri.fromFile(File(audioPath)),
                                                    messageType = "voice",
                                                    content = "", // 语音不含文本内容
                                                    senderId = currentUserId,
                                                    receiverId = peerId,
                                                    convId = convId,
                                                    isGroup = isGroup,
                                                ) {}
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("按住说话", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        // 文本输入框
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier
                                .weight(1f),
                            placeholder = {
                                Text(
                                    "输入消息...", fontSize = 14.sp, // 降低字体大小
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    // 更多功能（+号弹出工具栏）
                    IconButton(onClick = { isDialogOpen = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }

                    Spacer(Modifier.width(4.dp))

                    // 发送按钮
                    if (!isVoiceMode && messageText.text.isNotBlank()) {
                        // 输入框有文字时显示发送按钮
                        Button(onClick = {
                            viewModel.sendMessage(
                                context = context,
                                mediaUri = null,
                                messageType = "text",
                                content = messageText.text,
                                senderId = currentUserId,
                                receiverId = peerId,
                                convId = convId,
                                isGroup = isGroup,
                            ) { success ->
                                if (success) {
                                    viewModel.loadMessagesByConvId(context, convId)
                                    messageText = TextFieldValue()
                                }
                            }
                        }) {
                            Text("发送")
                        }
                    }

                }
            }

            // 弹出工具栏
            if (isDialogOpen) {
                AlertDialog(
                    onDismissRequest = { isDialogOpen = false },
                    title = { Text("选择功能") },
                    confirmButton = {},
                    text = {
                        Column {
                            TextButton(onClick = {
                                Toast.makeText(context, "选择照片（模拟）", Toast.LENGTH_SHORT).show()
                                isDialogOpen = false
                            }) {
                                Text("选择照片")
                            }
                            TextButton(onClick = {
                                Toast.makeText(context, "拍照（模拟）", Toast.LENGTH_SHORT).show()
                                isDialogOpen = false
                            }) {
                                Text("拍照")
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)

        ) {
            items(items = messages) { msg: MessageDisplay ->
                val isSelf = msg.sender_id == currentUserId
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 对方头像
                    if (!isSelf) {
                        val imageUrl = "$BASE_URL${peerAvatarUrl}"
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(Modifier.width(4.dp))
                    }

                    Column(
                        horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
                    ) {
                        // 群聊中显示用户名
                        if (isGroup && !isSelf) {
                            Text(
                                text = msg.sender_name,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                        // 气泡
                        val bubbleWidth = when (msg.message_type) {
                            "voice" -> {
                                // 用本地文件大小决定宽度，最大 200KB
                                val size = getFileSizeFromUrl(msg.media_url!!)
                                val maxSize = 200 * 1024L
                                val frac = (size.toFloat() / maxSize).coerceIn(0f, 1f)
                                minBubbleWidth + (maxBubbleWidth - minBubbleWidth) * frac
                            }

                            else -> maxBubbleWidth
                        }.coerceAtMost(maxBubbleWidth)

                        if (msg.message_type == "voice" && msg.media_url != null) {
                            // 独立显示语音消息
                            val durationSec = remember(msg.media_url) {
                                mutableIntStateOf(getDurationFromUrl("$BASE_URL${msg.media_url}"))
                            }
                            val width =
                                minBubbleWidth + (maxBubbleWidth - minBubbleWidth) * (durationSec.value / 60f).coerceIn(
                                    0f,
                                    1f
                                )

                            Card(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .width(width)
                                    .clickable {
                                        viewModel.playAudio(context, "$BASE_URL${msg.media_url}")
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "播放语音")
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "${durationSec.value}″",
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                            }

                        } else {
                            // 使用气泡显示文本消息
                            Surface(
                                shape = chatBubbleShape(isSelf),
                                color = if (isSelf)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .widthIn(min = minBubbleWidth, max = maxBubbleWidth)
                                    .wrapContentWidth()
                            ) {
                                Text(
                                    text = msg.content,
                                    color = Color.Black,
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
                                    ),
                                    fontSize = 16.sp
                                )
                            }
                        }

                    }

                    // 自己头像
                    if (isSelf) {
                        Spacer(Modifier.width(4.dp))
                        val imageUrl = "$BASE_URL${currentUserAvatarUrl}"

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "My Avatar",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }
}

fun getDurationFromUrl(url: String): Int {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(url, HashMap())
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        val durationMs = durationStr?.toLongOrNull() ?: 0L
        (durationMs / 1000).toInt()
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }
}


fun getFileSizeFromUrl(fileUrl: String): Long {
    return try {
        val url = URL(fileUrl)
        val connection = url.openConnection()
        connection.connect()
        connection.contentLengthLong // 获取文件大小（字节）
    } catch (e: Exception) {
        e.printStackTrace()
        0L // 获取失败时返回 0
    }
}


fun parseMessage(json: JSONObject): MessageDisplay {
    val rowGroup = json.getBoolean("is_group")
    return MessageDisplay(
        conv_id = json.getLong("conv_id"),
        sender_id = json.getLong("sender_id"),
        sender_name = json.getString("sender_name"),
        sender_avatar = json.getString("sender_avatar"),
        content = json.getString("content"),
        timestamp = json.getLong("timestamp"),
        is_group = if (rowGroup) 1 else 0,
        message_type = json.getString("message_type"),
        media_url = if (json.has("media_url") && !json.isNull("media_url")) json.getString("media_url") else null,
    )
}

@SuppressLint("SimpleDateFormat")
fun startRecording(context: Context) {
    val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
    val fileName = "REC_${SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())}.mp3"
    val outputFile = File(outputDir, fileName)
    audioFilePath = outputFile.absolutePath

    mediaRecorder = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setOutputFile(audioFilePath)
        prepare()
        start()
    }
}

fun stopRecording(onComplete: (String) -> Unit) {
    mediaRecorder?.apply {
        stop()
        release()
    }
    mediaRecorder = null
    audioFilePath?.let { onComplete(it) }
}


@Composable
fun chatBubbleShape(
    isSelf: Boolean,
    cornerDp: Dp = 12.dp,
    pointerDp: Dp = 8.dp
): Shape {
    val density = LocalDensity.current
    val cornerPx = with(density) { cornerDp.toPx() }
    val pointerPx = with(density) { pointerDp.toPx() }
    return GenericShape { size, _ ->
        val midY = size.height / 2f
        if (isSelf) {
            // 右侧气泡主体
            addRoundRect(
                RoundRect(
                    0f,
                    0f,
                    size.width - pointerPx,
                    size.height,
                    CornerRadius(cornerPx, cornerPx)
                )
            )
            // 尖角：三角心在 midY
            moveTo(size.width - pointerPx, midY - pointerPx)
            lineTo(size.width, midY)
            lineTo(size.width - pointerPx, midY + pointerPx)
            close()
        } else {
            // 左侧气泡主体
            addRoundRect(
                RoundRect(
                    pointerPx,
                    0f,
                    size.width,
                    size.height,
                    CornerRadius(cornerPx, cornerPx)
                )
            )
            // 尖角：三角心在 midY
            moveTo(pointerPx, midY - pointerPx)
            lineTo(0f, midY)
            lineTo(pointerPx, midY + pointerPx)
            close()
        }
    }
}
