package com.example.ochataku.ui
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.Info
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.*
//
//@Composable
//fun GroupChatScreen(
//    group: Group,
//    onBackClick: () -> Unit,
//    viewModel: GroupChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
//) {
//    val messageState by viewModel.messageState.collectAsState()
//    val listState = rememberLazyListState()
//
//    LaunchedEffect(messageState.messages.size) {
//        if (messageState.messages.isNotEmpty()) {
//            listState.animateScrollToItem(messageState.messages.size - 1)
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            GroupChatTopBar(
//                group = group,
//                onBackClick = onBackClick,
//                onGroupInfoClick = { /* 处理群信息点击 */ }
//            )
//        },
//        bottomBar = {
//            MessageInputField(
//                onSendMessage = { content ->
//                    viewModel.sendMessage(content)
//                }
//            )
//        }
//    ) { innerPadding ->
//        LazyColumn(
//            modifier = Modifier
//                .padding(innerPadding)
//                .fillMaxSize(),
//            state = listState,
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            items(messageState.messages) { message ->
//                ChatMessageItem(
//                    message = message,
//                    isCurrentUser = message.sender.id == "user_001" // 假设当前用户ID
//                )
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun GroupChatTopBar(
//    group: Group,
//    onBackClick: () -> Unit,
//    onGroupInfoClick: () -> Unit
//) {
//    CenterAlignedTopAppBar(
//        title = {
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Text(group.name)
//                Text(
//                    text = "${group.members.size} 成员",
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//        },
//        navigationIcon = {
//            IconButton(onClick = onBackClick) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                    contentDescription = "返回"
//                )
//            }
//        },
//        actions = {
//            IconButton(onClick = onGroupInfoClick) {
//                Icon(
//                    imageVector = Icons.Default.Info,
//                    contentDescription = "群信息"
//                )
//            }
//        }
//    )
//}
//
//@Composable
//private fun ChatMessageItem(
//    message: ChatMessage,
//    isCurrentUser: Boolean
//) {
//    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start
//    val bubbleColor = if (isCurrentUser) MaterialTheme.colorScheme.primary
//    else MaterialTheme.colorScheme.surfaceVariant
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        contentAlignment = alignment
//    ) {
//        Column(
//            horizontalAlignment = alignment,
//            modifier = Modifier
//                .widthIn(max = 280.dp)
//                .clip(RoundedCornerShape(12.dp))
//                .background(bubbleColor)
//                .padding(12.dp)
//        ) {
//            if (!isCurrentUser) {
//                Text(
//                    text = message.sender.name,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//            }
//            Text(
//                text = message.content,
//                color = if (isCurrentUser) Color.White
//                else MaterialTheme.colorScheme.onSurface
//            )
//            Text(
//                text = message.formattedTime,
//                style = MaterialTheme.typography.labelSmall,
//                color = if (isCurrentUser) Color.White.copy(alpha = 0.8f)
//                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//                modifier = Modifier.align(Alignment.End)
//            )
//        }
//    }
//}
//
//@Composable
//private fun MessageInputField(
//    onSendMessage: (String) -> Unit
//) {
//    var textState by remember { mutableStateOf(TextFieldValue("")) }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        OutlinedTextField(
//            value = textState,
//            onValueChange = { textState = it },
//            placeholder = { Text("输入消息...") },
//            modifier = Modifier
//                .weight(1f)
//                .padding(end = 8.dp),
//            singleLine = false,
//            maxLines = 3
//        )
//
//        IconButton(
//            onClick = {
//                val content = textState.text.trim()
//                if (content.isNotEmpty()) {
//                    onSendMessage(content)
//                    textState = TextFieldValue("")
//                }
//            }
//        ) {
//            Icon(
//                imageVector = Icons.Default.Send,
//                contentDescription = "发送",
//                tint = MaterialTheme.colorScheme.primary
//            )
//        }
//    }
//}
//
//// ViewModel
//class GroupChatViewModel : ViewModel() {
//    private val _messageState = mutableStateOf(GroupChatState())
//    val messageState get() = _messageState
//
//    init {
//        // 加载初始消息
//        loadInitialMessages()
//    }
//
//    private fun loadInitialMessages() {
//        val messages = listOf(
//            ChatMessage(
//                id = "1",
//                content = "大家觉得这个设计方案怎么样？",
//                sender = User("user_002", "设计师老王"),
//                timestamp = System.currentTimeMillis() - 3600000
//            ),
//            ChatMessage(
//                id = "2",
//                content = "我觉得配色可以再调整一下",
//                sender = User("user_003", "UI小李"),
//                timestamp = System.currentTimeMillis() - 1800000
//            )
//        )
//        _messageState.value = GroupChatState(messages = messages)
//    }
//
//    fun sendMessage(content: String) {
//        val newMessage = ChatMessage(
//            id = UUID.randomUUID().toString(),
//            content = content,
//            sender = User("user_001", "我"), // 当前用户
//            timestamp = System.currentTimeMillis()
//        )
//        _messageState.value = _messageState.value.copy(
//            messages = _messageState.value.messages + newMessage
//        )
//    }
//}
//
//// 数据类
//data class GroupChatState(
//    val messages: List<ChatMessage> = emptyList()
//)
//
//data class ChatMessage(
//    val id: String,
//    val content: String,
//    val sender: User,
//    val timestamp: Long
//) {
//    val formattedTime: String
//        get() = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
//}
//
//data class Group(
//    val id: String,
//    val name: String,
//    val members: List<User>,
//    val avatar: String
//)
//
//// 使用示例
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MaterialTheme {
//                GroupChatScreen(
//                    group = Group(
//                        id = "group_001",
//                        name = "产品设计讨论组",
//                        members = listOf(
//                            User("user_001", "我"),
//                            User("user_002", "设计师老王"),
//                            User("user_003", "UI小李")
//                        ),
//                        avatar = ""
//                    ),
//                    onBackClick = { finish() }
//                )
//            }
//        }
//    }
//}