// ✅ 整合导航：MainActivity.kt（无嵌套 NavController）
package com.example.ochataku.ui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.ui.chat.ChatScreen
import com.example.ochataku.ui.contact.AddFriendScreen
import com.example.ochataku.ui.contact.ContactProfileScreen
import com.example.ochataku.ui.contact.ContactScreen
import com.example.ochataku.ui.contact.FriendRequestScreen
import com.example.ochataku.ui.contact.GroupListScreen
import com.example.ochataku.ui.theme.ChatAppTheme
import com.example.ochataku.utils.PermissionUtils
import com.example.ochataku.viewmodel.LoginViewModel
import com.example.ochataku.viewmodel.MainViewModel
import com.example.ochataku.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authManager = AuthManager(applicationContext)


        setContent {
            ChatAppTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = hiltViewModel()
                val uiState by mainViewModel.uiState.collectAsState()

                // 登录状态导航控制
                LaunchedEffect(uiState) {
                    when (uiState) {
                        is MainViewModel.UiState.Loading -> {
                            navController.navigate("loading") { popUpTo(0) { inclusive = true } }
                        }

                        is MainViewModel.UiState.LoggedIn -> {
                            navController.navigate("conversation") {
                                popUpTo(0) { inclusive = true }
                            }
                        }

                        is MainViewModel.UiState.LoggedOut -> {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                val bottomRoutes = setOf("conversation", "contacts", "profile")
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute in bottomRoutes
                val currentUserId = authManager.getUserId()


                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController)
                        }
                    }
                ) { padding ->
                    val startDestination =
                        if (uiState is MainViewModel.UiState.LoggedIn) "conversation" else "loading"
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(padding)
                    ) {

                        composable("loading") { LoadingScreen() }

                        composable("login") {
                            val loginViewModel: LoginViewModel = hiltViewModel()
                            LoginScreen(
                                navController = navController,
                                viewModel = loginViewModel,
                                mainViewModel = mainViewModel
                            )
                        }

                        composable("register") {
                            val registerViewModel: RegisterViewModel = hiltViewModel()
                            RegisterScreen(
                                onBack = { navController.popBackStack() },
                                viewModel = registerViewModel
                            )
                        }

                        composable("conversation") {
                            // 1. 先拿到当前用户 ID
                            val currentUserId = authManager.getUserId()

                            // 2. 渲染会话列表，并传入 onConversationClick 回调
                            ConversationScreen(
                                userId = currentUserId,
                                onConversationClick = { convId, peerId, peerName, isGroup, peerAvatar ->
                                    // 3. 直接用一个路由区分私聊/群聊
                                    val encodedName = Uri.encode(peerName)
                                    val encodedUrl = Uri.encode(peerAvatar)
                                    navController.navigate("chat/$convId/$peerId/$encodedName/$isGroup/$encodedUrl")
                                }
                            )
                        }

                        composable(
                            route = "chat/{convId}/{peerId}/{peerName}/{isGroup}/{peerAvatar}",
                            arguments = listOf(
                                navArgument("convId") { type = NavType.LongType },
                                navArgument("peerId") { type = NavType.LongType },
                                navArgument("peerName") { type = NavType.StringType },
                                navArgument("isGroup") { type = NavType.BoolType },
                                navArgument("peerAvatar") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val convId = backStackEntry.arguments!!.getLong("convId")
                            val peerId = backStackEntry.arguments!!.getLong("peerId")
                            val peerName = backStackEntry.arguments!!.getString("peerName")!!
                            val isGroup = backStackEntry.arguments!!.getBoolean("isGroup")
                            // 解码回原始 URL
                            val mediaUrlEncoded =
                                backStackEntry.arguments!!.getString("peerAvatar")!!
                            val mediaUrl = Uri.decode(mediaUrlEncoded)
                            val currentUserId = AuthManager(LocalContext.current).getUserId()
                            val currentUserAvatar =
                                AuthManager(LocalContext.current).getUserAvatar()

                            ChatScreen(
                                navController = navController,
                                convId = convId,
                                peerId = peerId,
                                peerName = peerName,
                                isGroup = isGroup,
                                currentUserId = currentUserId,
                                peerAvatarUrl = mediaUrl,
                                currentUserAvatarUrl = currentUserAvatar
                            )
                        }

                        composable("contacts") {
                            ContactScreen(
                                navController = navController
                            )
                        }

                        composable("contact_profile/{userId}") { backStackEntry ->
                            val userId =
                                backStackEntry.arguments?.getString("userId")?.toLongOrNull()
                            userId?.let {
                                ContactProfileScreen(navController = navController, userId = it)
                            }
                        }


                        composable("profile") {
                            ProfileScreen()
                        }

                        composable("friend_request") {
                            FriendRequestScreen()
                        }

                        composable("add_friend") {
                            AddFriendScreen(
                                navController = navController,
                                currentUserId = currentUserId
                            )
                        }

                        composable("group_list") {
                            GroupListScreen(
                                userId = currentUserId,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        PermissionUtils.handlePermissionResult(this, requestCode, grantResults) {
            when (requestCode) {
                PermissionUtils.REQUEST_CODE_CAMERA -> {
                    // 相机权限已授权，可以执行拍照逻辑
                }

                PermissionUtils.REQUEST_CODE_AUDIO -> {
                    // 录音权限已授权
                }

                PermissionUtils.REQUEST_CODE_MEDIA -> {
                    // 相册权限已授权
                }
            }
        }
    }


}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, "Conversations") },
            label = { Text("会话") },
            selected = currentRoute == "conversation",
            onClick = {
                if (currentRoute != "conversation") {
                    navController.navigate("conversation") {
                        launchSingleTop = true
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.PhoneIphone, "Contacts") },
            label = { Text("通讯录") },
            selected = currentRoute == "contacts",
            onClick = {
                if (currentRoute != "contacts") {
                    navController.navigate("contacts") {
                        launchSingleTop = true
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, "Profile") },
            label = { Text("个人") },
            selected = currentRoute == "profile",
            onClick = {
                if (currentRoute != "profile") {
                    navController.navigate("profile") {
                        launchSingleTop = true
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        )

    }
}