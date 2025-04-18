package com.example.ochataku.ui

import android.widget.Toast
import com.example.ochataku.viewmodel.LoginViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ochataku.viewmodel.LoginState
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ochataku.viewmodel.MainViewModel
@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = hiltViewModel(), mainViewModel: MainViewModel) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                isLoading = false
                Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show()
                mainViewModel.refreshLoginStatus()
                navController.navigate("conversation"){
                    popUpTo(0) { inclusive = true }
                }
            }
            is LoginState.Error -> {
                isLoading = false
                errorMessage = (loginState as LoginState.Error).message
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "欢迎登录",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        LoginForm(
            username = username,
            password = password,
            errorMessage = errorMessage,
            onUsernameChange = { username = it },
            onPasswordChange = { password = it },
            focusRequester = focusRequester
        )

        Spacer(modifier = Modifier.height(24.dp))

        LoginButton(
            isLoading = isLoading,
            enabled = username.isNotBlank() && password.isNotBlank(),
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = null
                    viewModel.login(username, password)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("没有账号？立即注册")
        }
    }
}

@Composable
private fun LoginForm(
    username: String,
    password: String,
    errorMessage: String?,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("用户名") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("密码") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            isError = errorMessage != null
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

@Composable
private fun LoginButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text("登 录")
        }
    }
}