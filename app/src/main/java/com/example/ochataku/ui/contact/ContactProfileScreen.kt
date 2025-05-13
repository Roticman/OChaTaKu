package com.example.ochataku.ui.contact

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.ochataku.R
import com.example.ochataku.manager.AuthManager
import com.example.ochataku.service.ApiClient
import com.example.ochataku.service.ContactRequest
import com.example.ochataku.service.ConversationRequest
import com.example.ochataku.viewmodel.ContactProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactProfileScreen(navController: NavController, userId: Long) {
    val viewModel: ContactProfileViewModel = hiltViewModel()
    val profile by viewModel.profileState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "详细资料",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { padding ->
        if (profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 头像区域
                Card(
                    modifier = Modifier
                        .padding(24.dp)
                        .size(90.dp)
                        .shadow(8.dp, shape = CircleShape),
                    shape = CircleShape
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(ApiClient.BASE_URL + profile!!.avatar)
                            .crossfade(true)
                            .build(),
                        contentDescription = "头像",
                        placeholder = rememberAsyncImagePainter(R.drawable.default_avatar),
                        error = rememberAsyncImagePainter(R.drawable.default_avatar),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }

                // 基本信息卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileItem(
                            icon = R.drawable.ic_person,
                            label = "用户名",
                            value = profile!!.username
                        )
                        ProfileItem(
                            icon = R.drawable.ic_id,
                            label = "用户ID",
                            value = profile!!.userId.toString()
                        )
                        ProfileItem(
                            icon = R.drawable.ic_phone,
                            label = "手机",
                            value = profile!!.phone
                        )
                        ProfileItem(
                            icon = R.drawable.ic_email,
                            label = "邮箱",
                            value = profile!!.email
                        )
                        ProfileItem(
                            icon = R.drawable.ic_gender,
                            label = "性别",
                            value = when (profile!!.gender.toString()) {
                                "MALE" -> "男"
                                "FEMALE" -> "女"
                                else -> "未设置"
                            }
                        )
                        ProfileItem(
                            icon = R.drawable.ic_birthdate,
                            label = "生日",
                            value = profile!!.birthDate
                        )
                        ProfileItem(
                            icon = R.drawable.ic_location,
                            label = "地区",
                            value = profile!!.region
                        )
                        ProfileItem(
                            icon = R.drawable.ic_signature,
                            label = "个性签名",
                            value = profile!!.signature
                        )
                    }
                }

                // 操作按钮
                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val currentUserId = AuthManager(context).getUserId()
                                val request = ContactRequest(
                                    userId = currentUserId,
                                    peerId = profile!!.userId,
                                    isGroup = false
                                )
                                val response = ApiClient.apiService.getOrCreateConversation(request)
                                val convId = response.convId

                                // 构造参数并导航
                                val encodedName = Uri.encode(profile!!.username)
                                val encodedAvatar = Uri.encode(profile!!.avatar)
                                navController.navigate("chat/$convId/${profile!!.userId}/$encodedName/false/$encodedAvatar")
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "创建会话失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("发消息", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileItem(icon: Int, label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//            Text(
//                text = label,
//                style = MaterialTheme.typography.bodySmall.copy(
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    fontSize = 12.sp
//                )
//            )
            Text(
                text = "$label: ${value ?: "未填写"}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}