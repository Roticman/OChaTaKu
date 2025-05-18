package com.example.ochataku.ui.chat

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.ochataku.service.ApiClient.BASE_URL
import com.example.ochataku.service.MessageDisplay
import com.example.ochataku.viewmodel.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageList(
    messages: List<MessageDisplay>,
    listState: LazyListState,
    padding: PaddingValues,
    currentUserId: Long,
    currentUserAvatarUrl: String?,
    peerAvatarUrl: String?,
    isGroup: Boolean,
    context: Context,
    viewModel: ChatViewModel,
    navController: NavController
) {

    // 屏幕宽度，用于气泡最大宽度
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxBubbleWidth = screenWidth * 0.7f
    val minBubbleWidth = 64.dp
    LazyColumn(
        state = listState,
        contentPadding = padding,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .imePadding()
    ) {
        items(items = messages) { msg ->
            val isSelf = msg.sender_id == currentUserId

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isSelf) {
                    val avatarPath = if (isGroup) msg.sender_avatar else peerAvatarUrl
                    val imageUrl = "$BASE_URL${avatarPath}"
                    val profileRoute = "contact_profile/${msg.sender_id}"
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { navController.navigate(profileRoute) }
                    )
                    Spacer(Modifier.width(4.dp))
                }

                Column(horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start) {
                    if (isGroup && !isSelf) {
                        Text(
                            text = msg.sender_name,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    val mediaPath = "$BASE_URL${msg.media_url}"
                    val scope = rememberCoroutineScope()

                    when (msg.message_type) {
                        "text" -> {
                            Surface(
                                shape = chatBubbleShape(isSelf),
                                color = if (isSelf)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .widthIn(min = minBubbleWidth, max = maxBubbleWidth)
                                    .wrapContentWidth()
                                    .combinedClickable(
                                        onClick = {},
                                        onLongClick = {
                                            showMessageActionDialog(context, msg.id, viewModel, currentUserId)
                                        }
                                    )
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

                        "image" -> {
                            var showFullImage by remember { mutableStateOf(false) }
                            val painter = rememberAsyncImagePainter(model = mediaPath)
                            val bitmap =
                                (painter.state as? coil.compose.AsyncImagePainter.State.Success)?.result?.drawable
                            val bitmapWidth = bitmap?.intrinsicWidth ?: 1
                            val bitmapHeight = bitmap?.intrinsicHeight ?: 1

                            val size = calculateDisplaySize(bitmapWidth, bitmapHeight)

                            if (showFullImage) {
                                FullScreenMediaDialog(
                                    mediaUrl = mediaPath,
                                    mediaType = msg.message_type,
                                    onDismiss = { showFullImage = false },
                                    onSaveClick = {
                                        scope.launch {
                                            saveImageToGallery(context, mediaPath)
                                        }
                                    }
                                )
                            }

                            Image(
                                painter = painter,
                                contentDescription = "Image",
                                modifier = Modifier
                                    .size(width = size.width, height = size.height)
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = { showFullImage = true },
                                        onLongClick = {
                                            showMessageActionDialog(context, msg.id, viewModel, currentUserId)
                                        }
                                    ),

                                contentScale = ContentScale.Fit
                            )
                        }


                        "voice" -> {
                            val durationSec = remember(msg.media_url) {
                                mutableIntStateOf(getDurationFromUrl(mediaPath))
                            }
                            val width = minBubbleWidth + (maxBubbleWidth - minBubbleWidth) *
                                    (durationSec.intValue / 60f).coerceIn(0f, 1f)
                            Card(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .width(width)
                                    .combinedClickable(
                                        onClick = { viewModel.playAudio(context, mediaPath) },
                                        onLongClick = {
                                            showMessageActionDialog(context, msg.id, viewModel, currentUserId)
                                        }
                                    ),
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
                        }

                        "video" -> {
                            val thumbnail = remember(mediaPath) {
                                getVideoThumbnail(context, mediaPath)
                            }
                            val thumbnailWidth = thumbnail?.width ?: 1
                            val thumbnailHeight = thumbnail?.height ?: 1

                            val size = calculateDisplaySize(thumbnailWidth, thumbnailHeight)

                            var showFullScreen by remember { mutableStateOf(false) }

                            if (showFullScreen) {
                                FullScreenMediaDialog(
                                    mediaUrl = mediaPath,
                                    mediaType = "video",
                                    onDismiss = { showFullScreen = false },
                                    onSaveClick = {
                                        scope.launch {
                                            saveVideoToGallery(context, mediaPath)
                                        }
                                    }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(width = size.width, height = size.height)
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = { showFullScreen = true },
                                        onLongClick = {
                                            showMessageActionDialog(context, msg.id, viewModel, currentUserId)
                                        }
                                    ),
                            ) {
                                if (thumbnail != null) {
                                    Image(
                                        bitmap = thumbnail.asImageBitmap(),
                                        contentDescription = "视频封面",
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "播放视频",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

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
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                navController.navigate("profile")
                            }
                    )
                }
            }
        }
    }
}

fun showMessageActionDialog(context: Context, messageId: Long, viewModel: ChatViewModel, currentUserId: Long) {
    val userId = currentUserId  // ✅ 添加 getter 获取当前用户 ID
    val message = viewModel.messages.value.find { it.id == messageId }

    if (message == null) {
        Toast.makeText(context, "消息不存在", Toast.LENGTH_SHORT).show()
        return
    }

    val isSelf = message.sender_id == userId
    val options = if (isSelf) arrayOf("删除", "引用") else arrayOf("引用")

    AlertDialog.Builder(context)
        .setTitle("消息操作")
        .setItems(options) { _, which ->
            if (isSelf) {
                when (which) {
                    0 -> viewModel.deleteMessage(messageId)
                    1 -> viewModel.quoteMessage(messageId, context)
                }
            } else {
                when (which) {
                    0 -> viewModel.quoteMessage(messageId, context)
                }
            }
        }
        .setNegativeButton("取消", null)
        .show()
}


suspend fun saveImageToGallery(context: Context, url: String) {
    val request = ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false)
        .build()

    val result = context.imageLoader.execute(request)
    if (result is SuccessResult) {
        val bitmap = (result.drawable as BitmapDrawable).bitmap
        val filename = "image_${System.currentTimeMillis()}.jpg"

        val fos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            uri?.let { context.contentResolver.openOutputStream(it) }
        } else {
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val imageFile = File(imagesDir, filename)
            withContext(Dispatchers.IO) {
                FileOutputStream(imageFile)
            }
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(context, "已保存到相册", Toast.LENGTH_SHORT).show()
        }
    }
}

suspend fun saveVideoToGallery(context: Context, url: String) {
    withContext(Dispatchers.IO) {
        try {
            val input: InputStream = URL(url).openStream()

            val fileName = "video_${System.currentTimeMillis()}.mp4"
            val fos: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
                }
                val videoUri = context.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                videoUri?.let { context.contentResolver.openOutputStream(it) }
            } else {
                val moviesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                if (!moviesDir.exists()) moviesDir.mkdirs()
                val file = File(moviesDir, fileName)
                FileOutputStream(file)
            }

            fos?.use { output ->
                input.copyTo(output)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "视频已保存到相册", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "保存视频失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            e.printStackTrace()
        }
    }
}

fun getVideoThumbnail(context: Context, videoUrl: String): Bitmap? {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoUrl, HashMap())
        val bitmap = retriever.frameAtTime // 取第0秒的帧
        retriever.release()
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


data class Size(val width: Dp, val height: Dp)

fun calculateDisplaySize(
    bitmapWidth: Int,
    bitmapHeight: Int,
    maxWidth: Dp = 200.dp,
    maxHeight: Dp = 200.dp
): Size {
    if (bitmapWidth == 0 || bitmapHeight == 0) {
        return Size(maxWidth, maxHeight)
    }

    val aspectRatio = bitmapWidth.toFloat() / bitmapHeight.toFloat()
    val maxW = maxWidth.value
    val maxH = maxHeight.value

    return if (aspectRatio > 1f) {
        // 横图，宽优先
        val width = maxW
        val height = (maxW / aspectRatio).coerceAtMost(maxH)
        Size(width.dp, height.dp)
    } else {
        // 竖图，高优先
        val height = maxH
        val width = (maxH * aspectRatio).coerceAtMost(maxW)
        Size(width.dp, height.dp)
    }
}

