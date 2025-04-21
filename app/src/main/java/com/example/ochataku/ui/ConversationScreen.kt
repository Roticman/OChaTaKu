package com.example.ochataku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ochataku.R
import com.example.ochataku.service.ApiClient.BASE_URL
import com.example.ochataku.viewmodel.ConversationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    userId: Long,
    onConversationClick: (convId: Long, peerId: Long?, peerName: String, isGroup: Boolean, peerAvatar: String) -> Unit
) {
    val viewModel: ConversationViewModel = hiltViewModel()
    val conversations by viewModel.conversations.collectAsState()
    // 收集所有群成员头像列表的 Map<ConvId, List<AvatarUrl>>
    val groupMembersMap by viewModel.groupMembersMap.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadConversations(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .statusBarsPadding(),
                title = { Text("会话", fontSize = 18.sp) },
                colors = TopAppBarDefaults.smallTopAppBarColors()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(conversations) { convo ->
                // 如果群聊，提前加载成员数据
                if (convo.isGroup) {
                    LaunchedEffect(convo.convId) {
                        viewModel.loadGroupMembers(convo.convId)
                    }
                }
                val avatars = if (convo.isGroup) {
                    groupMembersMap[convo.convId] ?: emptyList()
                } else emptyList()

                ListItem(
                    leadingContent = {
                        if (convo.isGroup) {
                            GroupAvatarGrid(
                                avatarUrls = avatars,
                                size = 48.dp
                            )
                        } else {
                            val imageUrl = "${BASE_URL}${convo.avatar}"
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Avatar",
                                placeholder = painterResource(R.drawable.default_avatar),
                                error = painterResource(R.drawable.ic_avatar_error),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    },
                    headlineContent = {
                        val name = if (convo.isGroup) "[群] ${convo.name}" else convo.name
                        Text(name, fontSize = 16.sp)
                    },
                    supportingContent = {
                        Text(convo.lastMessage, fontSize = 14.sp, color = Color.Gray)
                    },
                    trailingContent = {
                        Text(
                            text = formatSmartTime(convo.timestamp),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val peerId = if (convo.isGroup) convo.groupId
                            else if (convo.aId != userId) convo.aId else convo.bId
                            onConversationClick(
                                convo.convId,
                                peerId,
                                convo.name,
                                convo.isGroup,
                                convo.avatar
                            )
                        }
                )
                Divider()
            }
        }
    }
}

/**
 * 将最多 9 个头像拼成网格
 */
@Composable
fun GroupAvatarGrid(
    avatarUrls: List<String>,
    size: androidx.compose.ui.unit.Dp
) {
    val urls = avatarUrls.take(9)
    val count = urls.size
    val cols = when {
        count <= 1 -> 1
        count <= 4 -> 2
        else -> 3
    }
    val rows = (count + cols - 1) / cols
    val itemSize = size / cols

    Column(
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        for (r in 0 until rows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                for (c in 0 until cols) {
                    val index = r * cols + c
                    if (index < count) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("${BASE_URL}${urls[index]}")
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(itemSize)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(itemSize)
                                .background(Color.LightGray)
                        )
                    }
                }
            }
        }
    }
}



fun formatSmartTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val msgTime = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(msgTime.time)
        }

        now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - msgTime.get(Calendar.DAY_OF_YEAR) == 1 -> {
            "昨天 " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(msgTime.time)
        }

        else -> {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(msgTime.time) // 星期几
        }
    }
}
