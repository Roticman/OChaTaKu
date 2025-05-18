package com.example.ochataku.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ochataku.R
import com.example.ochataku.service.ApiClient.BASE_URL
import com.example.ochataku.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(navController: NavController) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
    ) {
        // 头像区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 20.dp, start = 16.dp, end = 16.dp)
                .clickable { /* TODO: 点击头像区域处理 */ },
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${BASE_URL}${uiState.avatar}")
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                placeholder = painterResource(R.drawable.default_avatar),
                error = painterResource(R.drawable.ic_avatar_error),
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = uiState.username, // 这里动态换成用户名
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
                Text(
                    text = uiState.userId.toString(), // 这里动态换成账号
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Image(
                painter = painterResource(id = R.mipmap.ic_right),
                contentDescription = "Right Arrow",
                modifier = Modifier
                    .padding(top = 15.dp, end = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 账户安全
        OptionItem(
            iconRes = R.mipmap.ic_account,
            title = "账号与安全",
            onClick = {
                navController.navigate("account_security")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 消息通知
        OptionItem(
            iconRes = R.mipmap.ic_notification,
            title = "消息通知"
        )

        // 聊天背景 (隐藏，不显示)

        Spacer(modifier = Modifier.height(8.dp))

        // 文件、收藏
        OptionItem(
            iconRes = R.mipmap.ic_settings_file,
            title = "文件"
        )
        OptionItem(
            iconRes = R.mipmap.ic_star,
            title = "收藏"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 主题、语言、设置
        OptionItem(
            iconRes = R.mipmap.ic_theme,
            title = "主题"
        )
        OptionItem(
            iconRes = R.mipmap.ic_lang,
            title = "语言"
        )
        OptionItem(
            iconRes = R.mipmap.ic_setting,
            title = "设置"
        )
    }
}

@Composable
fun OptionItem(
    iconRes: Int,
    title: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = "Option Icon",
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        Image(
            painter = painterResource(id = R.mipmap.ic_right),
            contentDescription = "Arrow",
            modifier = Modifier.size(20.dp)
        )
    }

    Divider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
}