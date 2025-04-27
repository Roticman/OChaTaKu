package com.example.ochataku.ui.chat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ochataku.service.ApiClient.connectAndListen
import com.example.ochataku.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.io.File

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
    currentUserAvatarUrl: String?,
) {
    val viewModel: ChatViewModel = hiltViewModel()
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf(TextFieldValue()) }
    val listState = rememberLazyListState()
    var lastMessageCount by remember { mutableIntStateOf(0) }


    var showConfirmSendDialog by remember { mutableStateOf(false) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var firstLoadDone by remember { mutableStateOf(false) }


    val mediaPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            showConfirmSendDialog = true
        }
    }
    val imageCaptureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val file = File(context.cacheDir, "captured_image.jpg")
            file.outputStream().use { out -> it.compress(Bitmap.CompressFormat.JPEG, 100, out) }
            selectedMediaUri = Uri.fromFile(file)
            showConfirmSendDialog = true
        }
    }


// 录像
    val videoCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && selectedMediaUri != null) {
            showConfirmSendDialog = true
        }
    }
    val coroutineScope = rememberCoroutineScope()
    var showNewMessageHint by remember { mutableStateOf(false) }

    // 在 ChatScreen composable 里，和其他 launcher 并列
    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 裁剪后会把图片写到 Intent.EXTRA_OUTPUT（我们在 launchCrop 里指定）
            selectedMediaUri = result.data?.data  // 或者直接用之前传入的 destUri
            showConfirmSendDialog = true
        }
    }

    fun launchCrop(uri: Uri) {
        // 创建一个临时文件来接收裁剪结果
        val destFile = File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        val destUri = FileProvider.getUriForFile(
            context, "${context.packageName}.provider", destFile
        )
        val intent = Intent("com.android.camera.action.CROP").apply {
            setDataAndType(uri, "image/*")
            putExtra("crop", "true")
            putExtra("aspectX", 1)
            putExtra("aspectY", 1)
            putExtra("outputX", 800)
            putExtra("outputY", 800)
            putExtra("scale", true)
            putExtra(MediaStore.EXTRA_OUTPUT, destUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        cropImageLauncher.launch(intent)
    }
// ✅ 监听消息变化：首次加载滚动到底，后续控制新消息提示
    LaunchedEffect(messages) {
        val lastIndex = messages.lastIndex
        if (lastIndex >= 0) {
            delay(100) // 等待布局完成
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val isAtBottom = visibleItems.any { it.index == lastIndex }

            if (!firstLoadDone) {
                coroutineScope.launch {
                    listState.scrollToItem(lastIndex)
                }
                firstLoadDone = true
                showNewMessageHint = false
            } else if (!isAtBottom && messages.size > lastMessageCount) {
                showNewMessageHint = true
            } else {
                showNewMessageHint = false
            }

            lastMessageCount = messages.size
        }
    }

// ✅ 监听滚动位置，自动隐藏新消息提示
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastIndex = messages.lastIndex
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            visibleItems.any { it.index == lastIndex }
        }.distinctUntilChanged()
            .collectLatest { isAtBottom ->
                if (isAtBottom) showNewMessageHint = false
            }
    }

// ✅ 加载消息 + 接收消息流
    LaunchedEffect(convId) {
        viewModel.loadMessagesByConvId(context, convId)
        connectAndListen(convId) { messageJson ->
            val message = parseMessage(messageJson)
            coroutineScope.launch {
                viewModel.addMessage(message)
                delay(100)
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
    }


    Scaffold(
        topBar = {
            ChatTopBar(
                peerName = peerName,
                isGroup = isGroup,
                convId = convId,
                navController = navController
            )
        },
        bottomBar = {
            ChatInputBar(
                context = context,
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                coroutineScope = coroutineScope,
                listState = listState,
                messages = messages,
                viewModel = viewModel,
                currentUserId = currentUserId,
                peerId = peerId,
                convId = convId,
                isGroup = isGroup,
                mediaPickerLauncher = mediaPickerLauncher,
                imageCaptureLauncher = imageCaptureLauncher,
                videoCaptureLauncher = videoCaptureLauncher,
                selectedMediaUri = selectedMediaUri,
                onMediaUriSelected = { selectedMediaUri = it }
            )


        }
    ) { padding ->

        ChatMessageList(
            messages = messages,
            listState = listState,
            padding = padding,
            currentUserId = currentUserId,
            currentUserAvatarUrl = currentUserAvatarUrl,
            peerAvatarUrl = peerAvatarUrl,
            isGroup = isGroup,
            context = context,
            viewModel = viewModel
        )

        if (showNewMessageHint) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.lastIndex)
                        }
                        showNewMessageHint = false
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("有新消息")
                }
            }
        }
    }

    MediaEditDialog(
        showDialog = showConfirmSendDialog,
        selectedMediaUri = selectedMediaUri,
        onDismiss = { showConfirmSendDialog = false },
        onCrop = { uri -> launchCrop(uri) },
        onRetake = {
            showConfirmSendDialog = false
            imageCaptureLauncher.launch(null)
        },
        onReselect = {
            showConfirmSendDialog = false
            mediaPickerLauncher.launch("image/*")
        },
        onSend = {
            if (selectedMediaUri != null) {
                val contentResolver = context.contentResolver
                val type =
                    contentResolver.getType(selectedMediaUri!!) // MIME类型，比如 "image/jpeg"、"video/mp4"

                val messageType = when {
                    type?.startsWith("image") == true -> "image"
                    type?.startsWith("video") == true -> "video"
                    type?.startsWith("audio") == true -> "voice"
                    else -> "file" // 其他归为文件
                }

                viewModel.sendMessage(
                    context = context,
                    mediaUri = selectedMediaUri,
                    messageType = messageType,
                    content = "",
                    senderId = currentUserId,
                    receiverId = peerId,
                    convId = convId,
                    isGroup = isGroup
                ) {
                    // 成功回调
                    showConfirmSendDialog = false
                    // 这里可以执行更多成功后的操作，比如弹Toast、清空选择状态等
                }
            } else {
                // 没选择文件的情况，可以提醒用户
                Toast.makeText(context, "请先选择文件", Toast.LENGTH_SHORT).show()
            }
        }
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