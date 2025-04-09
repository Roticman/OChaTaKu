package com.example.ochataku.ui.chat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.ochataku.data.local.AppDatabase
import com.example.ochataku.viewmodel.*

class ChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val receiverId = intent.getLongExtra("receiver_id", -1L)
        if (receiverId == -1L) {
            Toast.makeText(this, "错误：未获取联系人 ID", Toast.LENGTH_SHORT).show()
            finish() // 关闭当前 Activity，防止错误操作
        } else {
            val database = AppDatabase.getDatabase(this)
            val chatViewModel by viewModels<ChatViewModel> {
                ChatViewModelFactory(database.privateMessageDao(), 1L) // 假设当前用户 ID 为 1
            }

            setContent {
                ChatScreen(chatViewModel, receiverId)
            }
        }

    }
}


