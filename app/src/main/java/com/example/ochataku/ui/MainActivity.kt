// ✅ 整合导航：MainActivity.kt（无嵌套 NavController）
package com.example.ochataku.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.ui.chat.ChatScreen
import com.example.ochataku.ui.contact.ContactScreen
import com.example.ochataku.ui.contact.FriendRequestScreen
import com.example.ochataku.ui.theme.ChatAppTheme
import com.example.ochataku.viewmodel.LoginViewModel
import com.example.ochataku.viewmodel.MainViewModel
import com.example.ochataku.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authManager = AuthManager(applicationContext)
        requestAllRequiredPermissions()
//        WindowCompat.setDecorFitsSystemWindows(window, false)


        setContent {
            ChatAppTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = hiltViewModel()
                val uiState by mainViewModel.uiState.collectAsState()

                // 登录状态导航控制
                LaunchedEffect(uiState) {
                    when (uiState) {
                        is MainViewModel.UiState.Loading -> {
                            navController.navigate("loading") { popUpTo(0) }
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
//
                        composable("profile") {
                            ProfileScreen()
                        }

                        composable("friend_request") {
                            FriendRequestScreen()
                        }
                    }
                }
            }
        }
    }

    private fun requestAllRequiredPermissions() {
        val permissions = mutableListOf<String>()

        permissions.add(Manifest.permission.RECORD_AUDIO)
        permissions.add(Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT >= 33) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= 28) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 100)
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
                    }
                }
            }
        )

    }
}