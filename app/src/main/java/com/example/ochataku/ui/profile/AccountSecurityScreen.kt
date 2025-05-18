package com.example.ochataku.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ochataku.viewmodel.AccountSecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSecurityScreen(navController: NavController) {
    val viewModel: AccountSecurityViewModel = hiltViewModel()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账号安全") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 修改密码卡片
            SecurityActionCard(
                icon = Icons.Default.Lock,
                title = "修改密码",
                description = "定期修改密码可提高账号安全性",
                buttonText = "修改",
                buttonColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    navController.navigate("change_password")
                }
            )

            // 退出登录卡片
            SecurityActionCard(
                icon = Icons.Default.Logout,
                title = "退出登录",
                description = "退出当前账号，但保留账号数据",
                buttonText = "退出",
                buttonColor = MaterialTheme.colorScheme.secondary,
                onClick = {
                    viewModel.logout {
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 注销账号卡片 - 更显眼的警告样式
            SecurityActionCard(
                icon = Icons.Default.PersonRemove,
                title = "注销账号",
                description = "永久删除账号及所有数据，操作不可撤销",
                buttonText = "注销",
                buttonColor = MaterialTheme.colorScheme.error,
                onClick = {
                    viewModel.showDeleteDialog()
                }

            )
        }

        val showDialog by viewModel.showDeleteAccountDialog.collectAsState()
        // 注销账号确认对话框
        if (showDialog) {
            DeleteAccountConfirmationDialog(
                onConfirm = {
                    viewModel.deleteAccount {
                        navController.navigate("login") {
                            popUpTo(0)
                        }
                    }
                },
                onDismiss = {
                    viewModel.dismissDeleteDialog()
                }

            )
        }
    }
}

@Composable
fun SecurityActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
fun DeleteAccountConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "确认注销账号",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "此操作将永久删除您的账号及所有数据，且无法恢复。确定要继续吗？",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("确认注销")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("取消")
            }
        }
    )
}