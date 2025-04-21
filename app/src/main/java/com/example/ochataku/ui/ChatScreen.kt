package com.example.ochataku.ui

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
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
                title = { Text(if (isGroup) "群聊 $peerName" else "私聊 $peerName") },
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息...") }
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (messageText.text.isNotBlank()) {
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
                                // 发送成功后重新拉取
                                viewModel.loadMessagesByConvId(context, convId)
                                messageText = TextFieldValue()
                            }
                        }
                    }
                }) {
                    Text("发送")
                }
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
                    verticalAlignment = Alignment.Bottom
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
                            if (msg.message_type == "voice") {
                                Text(
                                    text = "语音 ${getFileSizeFromUrl(msg.media_url!!) / 1024}KB",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 16.sp
                                )
                            } else {
                                Text(
                                    text = msg.content,
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
