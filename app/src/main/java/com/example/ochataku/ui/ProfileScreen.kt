package com.example.ochataku.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.ochataku.model.Auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    auth: Auth,
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("个人中心") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 头像区域
            ProfileHeader(
                auth = auth,
                modifier = Modifier.padding(16.dp),
                onEditClick = onEditProfile
            )

            // 个人信息卡片
            UserInfoCard(auth = auth, modifier = Modifier.padding(horizontal = 16.dp))

            // 设置列表
            SettingsList(
                modifier = Modifier.padding(top = 24.dp),
                onLogoutClick = { showLogoutDialog = true }
            )

            // 退出登录对话框
            if (showLogoutDialog) {
                LogoutDialog(
                    onDismiss = { showLogoutDialog = false },
                    onConfirm = onLogout
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    auth: Auth,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = rememberAsyncImagePainter({ auth.avatarUrl }),
                contentDescription = "用户头像",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = auth.username,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = auth.email,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 编辑按钮
        IconButton(
            onClick = onEditClick,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "编辑资料"
            )
        }
    }
}

@Composable
private fun UserInfoCard(auth: Auth, modifier: Modifier = Modifier) {
    var isEditing by remember { mutableStateOf(false) }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EditableListItem(
                icon = Icons.Default.Person,
                label = "昵称",
                value = auth.username,
                isEditing = isEditing
            )

            EditableListItem(
                icon = Icons.Default.Info,
                label = "个人简介",
                value = auth.bio,
                isEditing = isEditing
            )

            EditableListItem(
                icon = Icons.Default.Phone,
                label = "邮箱",
                value = auth.email,
                isEditing = isEditing
            )
        }
    }
}

@Composable
private fun EditableListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String?,
    isEditing: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        if (isEditing) {
            value?.let {
                TextField(
                    value = it,
                    onValueChange = { /* 更新逻辑 */ },
                    label = { Text(label) },
                    singleLine = true
                )
            }
        } else {
            Column {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                value?.let {
                    Text(
                        text = it.ifEmpty { "哈吉米，你这家伙..." },
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsList(
    modifier: Modifier = Modifier,
    onLogoutClick: () -> Unit
) {
    Column(modifier = modifier) {
        ListItem(
            headlineContent = { Text("账号与安全") },
            leadingContent = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null
                )
            }
        )

        ListItem(
            headlineContent = { Text("通知设置") },
            leadingContent = {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null
                )
            }
        )

        ListItem(
            headlineContent = { Text("隐私设置") },
            leadingContent = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ListItem(
            headlineContent = { Text("退出登录") },
            leadingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            modifier = Modifier.clickable { onLogoutClick() }
        )
    }
}

@Composable
private fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认退出登录？") },
        text = { Text("退出后需要重新登录才能访问个人内容") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
