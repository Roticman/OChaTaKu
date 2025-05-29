package com.example.ochataku.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ConversationScreen(
    userId: Long,
    onConversationClick: (convId: Long, peerId: Long?, peerName: String, isGroup: Boolean, peerAvatar: String) -> Unit
) {
    val viewModel: ConversationViewModel = hiltViewModel()

    val conversations by viewModel.conversations.collectAsState()
    val context = LocalContext.current
    // 收集所有群成员头像列表的 Map<ConvId, List<AvatarUrl>>
    val groupMembersMap by viewModel.groupMembersMap.collectAsState()
    val groupAvatarMap by viewModel.groupAvatarMap.collectAsState()
    val unreadConvs by viewModel.unreadConversations



    LaunchedEffect(userId) {
        viewModel.loadConversations(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .statusBarsPadding(),
                title = { Text(stringResource(R.string.conversation), fontSize = 18.sp) },
                colors = TopAppBarDefaults.smallTopAppBarColors()
            )
        }
    ) { padding ->
        var showDialog by remember { mutableStateOf(false) }
        var selectedConvId by remember { mutableStateOf<Long?>(null) }

        if (showDialog && selectedConvId != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteConversation(selectedConvId!!)
                        showDialog = false
                    }) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                title = { Text("删除会话") },
                text = { Text("确定要删除该会话及所有聊天记录吗？") }
            )
        }


        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(conversations) { convo ->

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            val peerId = if (convo.isGroup) convo.groupId
                            else if (convo.aId != userId) convo.aId else convo.bId
                            onConversationClick(
                                convo.convId,
                                peerId,
                                convo.name,
                                convo.isGroup,
                                convo.avatar
                            )
                        },
                        onLongClick = {
                            selectedConvId = convo.convId
                            showDialog = true
                        }
                    )) {
                    ListItem(
                        leadingContent = {
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
                            if (unreadConvs.contains(convo.convId)) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color.Red, RoundedCornerShape(4.dp))
                                )
                            }

                        },
                        headlineContent = {
                            val name =
                                if (convo.isGroup) "[${stringResource(R.string.group)}] ${convo.name}" else convo.name
                            Text(name, fontSize = 16.sp)
                        },
                        supportingContent = {
                            Text(
                                text = convo.lastMessage,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis // 👈 添加这两行
                            )
                        },
                        trailingContent = {
                            Box {
                                Text(
                                    text = formatSmartTime(context, convo.timestamp),
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                if (unreadConvs.contains(convo.convId)) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(Color.Red)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    Divider()
                }


            }
        }
    }
}


fun formatSmartTime(context: Context, timestamp: Long): String {
    val now = Calendar.getInstance()
    val msgTime = Calendar.getInstance().apply { timeInMillis = timestamp }

    // 使用 App 当前的 Locale（受 DataStore 设置影响）
    val locale = context.resources.configuration.locales[0]

    return when {
        // 今天
        now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("HH:mm", locale).format(msgTime.time)
        }

        // 昨天
        now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - msgTime.get(Calendar.DAY_OF_YEAR) == 1 -> {
            context.getString(R.string.yesterday) + " " + SimpleDateFormat("HH:mm", locale).format(
                msgTime.time
            )
        }

        // 其他（星期几）
        else -> {
            SimpleDateFormat("EEEE", locale).format(msgTime.time)
        }
    }
}

