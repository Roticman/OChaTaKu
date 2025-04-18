package com.example.ochataku.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.ochataku.R
import com.example.ochataku.viewmodel.ConversationViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    userId: Long,
    onConversationClick: (peerId: Long, isGroup: Boolean) -> Unit
) {
    val viewModel: ConversationViewModel = hiltViewModel()
    val conversations by viewModel.conversations.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadConversations(userId)
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("会话") })
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(conversations) { convo ->
                ListItem(
                    leadingContent = {
                        if (convo.avatar?.isNotBlank() == true) {
                            AsyncImage(
                                model = convo.avatar,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.default_avatar),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    },
                    headlineContent = {
                        (if (convo.isGroup) "[群] ${convo.peerName}" else convo.peerName)?.let {
                            Text(
                                it
                            )
                        }
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
                            onConversationClick(convo.peerId, convo.isGroup)
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
