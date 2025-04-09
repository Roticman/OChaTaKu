package com.example.ochataku.ui

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.ochataku.model.Contact
import com.example.ochataku.ui.chat.ChatActivity
import com.example.ochataku.viewmodel.ContactsViewModel


@OptIn(ExperimentalMaterial3Api::class) //仅对单个 Composable 组件使用实验 API
@Composable
fun ContactsScreen(
//    onLogout: ()->Unit,
    contactsViewModel: ContactsViewModel, // 传入 ViewModel
    navigateToChat: (Contact) -> Unit, // 传入跳转函数
    navigateToProfile: () -> Unit
) {
    // 监听 ViewModel 中的 contacts，UI 自动更新
    val contacts by contactsViewModel.contacts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("联系人") })
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            modifier = Modifier.fillMaxSize()
        ) {
            items(contacts) { contact -> // 这里用 `contacts` 而不是 `contact`
                ContactItem(contact) {
                    navigateToChat(contact) // 直接传递 `contact`
                }
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact, onClick: (Contact) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(8.dp)
            .clickable { onClick(contact) }
            .drawBehind {
                drawLine(
                    color = Color.LightGray, // 边框颜色
                    start = Offset(180f, size.height), // 起点：左下角
                    end = Offset(size.width, size.height), // 终点：右下角
                    strokeWidth = 1.dp.toPx() // 线条宽度
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        Image(
            painter = rememberAsyncImagePainter({ contact.avatarUrl }),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 名字 + 电话
        Column {
            Text(text = contact.username, style = MaterialTheme.typography.bodyLarge)
            Text(text = contact.phoneNumber, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

fun NavigateToChat(contact: Contact, context: Context) {
    val intent = Intent(context, ChatActivity::class.java).apply {
        putExtra("receiver_id", contact.userId)  // 传递联系人 ID
        Log.d("NavigateToChat", "传递的 receiver_id: ${contact.userId}")
        putExtra("receiver_name", contact.username)  // 传递联系人姓名（可选）
    }
    context.startActivity(intent)  // 启动 ChatActivity
}

