package com.example.ochataku.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.ochataku.R
import com.example.ochataku.viewmodel.RegisterViewModel
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var inputErrors by remember { mutableStateOf<Map<String, String?>>(emptyMap()) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> avatarUri = uri }

    val registerSuccess by viewModel.registerSuccess.collectAsState()

    LaunchedEffect(registerSuccess) {
        registerSuccess?.let {
            if (it) {
                Toast.makeText(context, "注册成功！", Toast.LENGTH_SHORT).show()
                onBack()
            } else {
                Toast.makeText(context, "注册失败，用户名可能已存在", Toast.LENGTH_SHORT).show()
            }
            viewModel.resetState()
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像选择
            AvatarPicker(avatarUri, pickImageLauncher)

            // 用户名输入
            InputField(
                value = username,
                onValueChange = { username = it },
                label = "用户名",
                errorMessage = inputErrors["username"],
                modifier = Modifier.fillMaxWidth()
            )

            // 邮箱输入
//            InputField(
//                value = email,
//                onValueChange = { email = it },
//                label = "邮箱",
//                errorMessage = inputErrors["email"],
//                modifier = Modifier.fillMaxWidth()
//            )

            // 密码输入
            InputField(
                value = password,
                onValueChange = { password = it },
                label = "密码",
                errorMessage = inputErrors["password"],
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            // 注册按钮
            Button(

                onClick = {

                    inputErrors = validateInputsWithMessages(username, password)
                    if (inputErrors.all { it.value == null }) {
                        viewModel.registerUser(
                            context = context,
                            username = username,
                            password = password,
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

// 带错误信息的验证函数
fun validateInputsWithMessages(
    username: String,
//    email: String,
    password: String
): Map<String, String?> {
    val errors = mutableMapOf<String, String?>()

    when {
        username.isBlank() -> errors["username"] = "用户名不能为空"
        username.length < 4 -> errors["username"] = "用户名至少4个字符"
        username.length > 20 -> errors["username"] = "用户名不能超过20个字符"
        !username.matches(Regex("^[a-zA-Z0-9_]+$")) ->
            errors["username"] = "只能包含字母、数字和下划线"

        username.matches(Regex("^\\d+$")) -> // 👈 新增这一行
            errors["username"] = "用户名不能全为数字"
    }

    when {
        password.isBlank() -> errors["password"] = "密码不能为空"
        password.length < 8 -> errors["password"] = "密码至少8个字符"
        !password.any { it.isDigit() } -> errors["password"] = "至少包含一个数字"
        !password.any { it.isLetter() } -> errors["password"] = "至少包含一个字母"
    }

    return errors
}

private fun hashPassword(password: String): String {
    // 复用之前的 SHA-256 哈希逻辑
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

// 头像选择组件
@Composable
private fun AvatarPicker(
    avatarUri: Uri?,
    pickImageLauncher: ManagedActivityResultLauncher<String, Uri?>
) {
    Box(contentAlignment = Alignment.BottomEnd) {
        Image(
            painter = rememberAsyncImagePainter(avatarUri ?: R.drawable.default_avatar),
            contentDescription = "用户头像",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable { pickImageLauncher.launch("image/*") },
            contentScale = ContentScale.Crop
        )
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "修改头像",
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .padding(4.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// 通用输入框组件
@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            visualTransformation = visualTransformation,
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}