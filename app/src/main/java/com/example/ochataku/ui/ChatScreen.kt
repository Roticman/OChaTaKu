// ✅ ChatScreen.kt（集成 ChatViewModel）
package com.example.ochataku.ui

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ochataku.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    convId: Long,
    peerName: String,
    isGroup: Boolean,
    currentUserId: Long,
//    peerAvatarUrl: String?,     // 可用在后续头像展示
//    currentUserAvatarUrl: String?
) {
    val viewModel: ChatViewModel = hiltViewModel()
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var messageText by remember { mutableStateOf(TextFieldValue()) }

    // ✅ 加载消息改为按 convId
    LaunchedEffect(convId) {
        viewModel.loadMessagesByConvId(convId)
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
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (messageText.text.isNotBlank()) {
                        viewModel.sendMessageWithOptionalFile(
                            context = context,
                            uri = null,
                            messageType = "text",
                            content = messageText.text,
                            senderId = currentUserId,
                            convId = convId,
                            isGroup = isGroup
                        ) { success ->
                            if (success) {
                                viewModel.loadMessagesByConvId(convId)
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
            items(messages) { msg ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (msg.senderId == currentUserId) Alignment.End else Alignment.Start
                ) {
                    if (isGroup && msg.senderId != currentUserId) {
                        Text(
                            text = msg.name ?: "成员",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                        )
                    }
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (msg.senderId == currentUserId) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = msg.content,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
