// ✅ 整合导航：MainActivity.kt（无嵌套 NavController）
package com.example.ochataku.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.ui.theme.ChatAppTheme
import com.example.ochataku.viewmodel.*
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
                            ConversationScreen(
                                userId = authManager.getUserId(),
                                onConversationClick = { peerId, isGroup ->
                                    val route =
                                        if (isGroup) "groupChat/$peerId" else "privateChat/$peerId"
                                    navController.navigate(route)
                                }
                            )
                        }

//                        composable("privateChat/{peerId}") { backStackEntry ->
//                            val peerId = backStackEntry.arguments?.getString("peerId")?.toLongOrNull() ?: return@composable
//                            ChatScreen(peerId = peerId, isGroup = false)
//                        }
//
//                        composable("groupChat/{peerId}") { backStackEntry ->
//                            val peerId = backStackEntry.arguments?.getString("peerId")?.toLongOrNull() ?: return@composable
//                            ChatScreen(peerId = peerId, isGroup = true)
//                        }

//                        composable("contacts") {
//                            // TODO: 联系人界面
//                        }
//
//                        composable("profile") {
//                            // TODO: 个人中心界面
//                        }
                    }
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
                    }
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, "Contacts") },
            label = { Text("通讯录") },
            selected = currentRoute == "contacts",
            onClick = { if (currentRoute != "contacts") {
                navController.navigate("contacts") {
                    launchSingleTop = true
                }
            } }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, "Profile") },
            label = { Text("个人") },
            selected = currentRoute == "profile",
            onClick = { if (currentRoute != "profile") {
                navController.navigate("profile") {
                    launchSingleTop = true
                }
            } }
        )
    }
}