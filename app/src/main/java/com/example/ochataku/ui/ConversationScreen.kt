package com.example.ochataku.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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

    LaunchedEffect(userId) {
        viewModel.loadConversations(userId)
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("会话") })
    }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(conversations) { convo ->
                val imageUrl = "${BASE_URL}${convo.avatar}"

                ListItem(
                    leadingContent = {
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
                    },
                    headlineContent = {
                        val name = if (convo.isGroup) "[群] ${convo.name}" else convo.name
                        Text(name)
                    },
                    supportingContent = {
                        Text(convo.lastMessage)
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
                            // ✅ 使用 conversation.id 作为 convId
                            val peerId = if (convo.isGroup) {
                                convo.groupId
                            } else {
                                if (convo.aId != userId) {
                                    convo.aId
                                } else {
                                    convo.bId
                                }
                            }
                            onConversationClick(
                                convo.convId,
                                peerId,
                                convo.name,
                                convo.isGroup,
                                convo.avatar
                            )
//                            Log.d("convIIIIIIIIIIIIIID", convo.convId.toString())
                        }
                )

                Divider()
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
