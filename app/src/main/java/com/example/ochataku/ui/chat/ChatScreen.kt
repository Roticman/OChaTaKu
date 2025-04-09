package com.example.ochataku.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ochataku.data.local.privatemessage.PrivateMessageEntity
import com.example.ochataku.viewmodel.ChatViewModel

@Composable
fun ChatScreen(chatViewModel: ChatViewModel, receiverId: Long) {
    val messages by chatViewModel.messages.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(messages) { message ->
                MessageItem(message)
            }
        }

        var input by remember { mutableStateOf("") }
        Row {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入消息...") }
            )
            Button(onClick = {
                if (input.isNotBlank()) {
                    chatViewModel.sendMessage(receiverId, input)
                    input = ""
                }
            }) {
                Text("发送")
            }
        }
    }
}

@Composable
fun MessageItem(message: PrivateMessageEntity) {
    Text(text = message.content, modifier = Modifier.padding(8.dp))
}


