package com.example.ochataku.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ochataku.model.ChatItem
import com.example.ochataku.viewmodel.ChatListViewModel

@Composable
fun ChatListScreen(
    chatListViewModel: ChatListViewModel,
    navigateToChat: (Long) -> Unit
) {
    val chatList by chatListViewModel.chatList.collectAsState()

    Column {
        Text("Chat List", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        LazyColumn {
            items(chatList) { chat ->
                ChatListItem(chat, navigateToChat)
            }
        }
    }
}

@Composable
fun ChatListItem(chat: ChatItem, navigateToChat: (Long) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navigateToChat(chat.id) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(chat.avatarUrl),
            contentDescription = "Avatar",
            modifier = Modifier.size(40.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(chat.name, fontWeight = FontWeight.Bold)
            Text(chat.lastMessage, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

