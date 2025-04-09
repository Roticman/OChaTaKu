package com.example.ochataku.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.ochataku.R
import com.example.ochataku.data.local.user.UserEntity
import com.example.ochataku.ui.main.validateInputs
import com.example.ochataku.viewmodel.RegisterViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import java.security.MessageDigest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        avatarUri = uri
    }

    val registerSuccess by viewModel.registerSuccess.collectAsState()

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show()
            onBack() // 注册成功后返回上一界面（或其他导航逻辑）
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用户注册") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(avatarUri ?: R.drawable.ic_avatar),
                contentDescription = "选择头像",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .clickable { pickImageLauncher.launch("image/*") }
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") }
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation()
            )

            Button(
                onClick = {
                    if (validateInputs(username, email, password)) {
                        viewModel.registerUser(
                            username = username,
                            email = email,
                            passwordHash = hashPassword(password),
                            avatarUri = avatarUri
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("立即注册")
            }
        }
    }
}

//fun onRegisterSuccess(username: String, email: String, password: String,avatarUri: String): UserEntity {
//    return UserEntity(
//        username = username,
//        email = email,
//        passwordHash = hashPassword(password), // 你应该实现 hash 函数
//        avatarUrl = avatarUri // 默认没有头像，可后续编辑添加
//    )
//}

//用 SHA-256 加密
fun hashPassword(password: String): String {
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(password.toByteArray())

    return bytes.joinToString("") { "%02x".format(it) }
}
