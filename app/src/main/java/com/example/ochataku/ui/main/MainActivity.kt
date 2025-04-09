// MainActivity.kt
package com.example.ochataku.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.ochataku.R
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.ui.*
import com.example.ochataku.ui.theme.ChatAppTheme
import com.example.ochataku.viewmodel.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val authManager by lazy { AuthManager(this) }

    private val contactsViewModel: ContactsViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val chatListViewModel: ChatListViewModel by viewModels()
    private val registerViewModel: RegisterViewModel by viewModels()
    private val mainViewModel by viewModels<MainViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(authManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatAppTheme {
                val uiState by mainViewModel.uiState.collectAsState()

                when (val state = uiState) {
                    is MainViewModel.UiState.Loading -> {
                        LoadingScreen()
                    }
                    is MainViewModel.UiState.LoggedIn -> {
                        val navController = rememberNavController()
                        AppNavigation(
                            navController = navController,
                            mainViewModel = mainViewModel,
                            chatListViewModel = chatListViewModel, // ✅ 只有登录状态才显示
                            contactsViewModel = contactsViewModel,
                            chatViewModel = chatViewModel,
                            showBottomBar = true, // ✅ 只有登录状态才显示
                        )
                    }
                    is MainViewModel.UiState.LoggedOut -> {
                        val navController = rememberNavController()
                        AuthNavigation(navController = navController)
                    }

                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray), // 背景颜色，防止加载图片时白屏
        contentAlignment = Alignment.Center
    ) {
        // 背景图片
        Image(
            painter = painterResource(id = R.drawable.loading_background), // 替换为你的图片资源
            contentDescription = "Loading Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // 让图片铺满屏幕
        )

        // 加载指示器
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp),
            color = Color.White, // 可调整颜色以匹配背景
            strokeWidth = 4.dp
        )
    }
}


@Composable
fun AppNavigation(
    navController: NavHostController, // 导航控制器
    mainViewModel: MainViewModel,         // 主界面的 ViewModel
    chatListViewModel: ChatListViewModel, // 聊天列表 ViewModel
    contactsViewModel: ContactsViewModel, // 联系人界面的 ViewModel
    chatViewModel: ChatViewModel, // 聊天界面 ViewModel
    showBottomBar: Boolean // 是否显示底部导航栏
) {
    Column {
        // 只有 `showBottomBar` 为 true 时才显示底部导航栏
        if (showBottomBar) {
            BottomNavigationBar(navController)
        }

        NavHost(
            navController = navController,
            startDestination = "chatList" // 默认显示聊天列表
        ) {
            // ✅ 聊天列表界面
            composable("chatList") {
                ChatListScreen(
                    chatListViewModel = chatListViewModel,
                    navigateToChat = { contactId ->
                        navController.navigate("chat/$contactId") // 点击进入聊天界面
                    }
                )
            }

            // ✅ 联系人界面
            composable("contacts") {
                ContactsScreen(
                    contactsViewModel = contactsViewModel,
                    navigateToChat = { contact ->
                        navController.navigate("chat/${contact.userId}") // 进入聊天界面
                    },
                    navigateToProfile = {
                        navController.navigate("profile") // 进入个人中心
                    }
                )
            }

            // ✅ 个人中心界面
            composable("profile") {
                ProfileScreen(
                    auth = mainViewModel.getAuth(), // 假设 `viewModel` 有 `currentUser`
                    onBackClick = { navController.popBackStack() },
                    onEditProfile = { /* 处理编辑个人资料逻辑 */ },
                    onLogout = {
                        mainViewModel.handleLogout()
                        navController.navigate("auth") {
                            popUpTo(0) // 清空导航栈
                        }
                    }
                )
            }

            // ✅ 单独的聊天界面（不在底部导航）
//            composable("chat/{contactId}") { backStackEntry ->
//                val contactId = backStackEntry.arguments?.getString("contactId")?.toLongOrNull() ?: 0L
//                ChatScreen(
//                    chatViewModel = chatViewModel,
//                    receiverId = contactId, // 传递接收者 ID
//                    onBack = { navController.popBackStack() } // 返回聊天列表
//                )
//            }
        }
    }
}

@Composable
fun AuthNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}



fun validateInputs(username: String, email: String, password: String): Boolean {
    return username.isNotBlank() && email.contains("@") && password.length >= 6
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar{
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "chatList") },
            label = { Text("Contacts") },
            selected = false,
            onClick = { navController.navigate("contacts") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "contacts") },
            label = { Text("Contacts") },
            selected = false,
            onClick = { navController.navigate("contacts") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "profile") },
            label = { Text("Profile") },
            selected = false,
            onClick = { navController.navigate("profile") }
        )
    }
}
