package com.example.ochataku.ui

//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.painter.Painter
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.ochataku.R
//
//
//@Composable
//fun ProfileScreen(
//    onProfileClick: () -> Unit = {},
//    onAccountSecurityClick: () -> Unit = {},
//    onNotificationClick: () -> Unit = {},
//    onFileClick: () -> Unit = {},
//    onFavoritesClick: () -> Unit = {},
//    onThemeClick: () -> Unit = {},
//    onLanguageClick: () -> Unit = {},
//    onSettingsClick: () -> Unit = {}
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//            .navigationBarsPadding()
//            .background(MaterialTheme.colorScheme.surface)
//    ) {
//        // 1. 用户信息头部
//        ProfileHeader(
//            avatar = painterResource(id = R.mipmap.avatar_def),
//            name = "wfc",
//            account = stringResource(id = R.string.my_chat_account),
//            onClick = onProfileClick
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // 2. 账号安全选项
//        OptionItem(
//            icon = painterResource(id = R.mipmap.ic_account),
//            title = stringResource(id = R.string.account_security),
//            onClick = onAccountSecurityClick,
//            showDivider = false,
//            modifier = Modifier.padding(top = 16.dp)
//        )
//
//        // 3. 消息通知分组
//        Column(
//            modifier = Modifier.padding(top = 16.dp)
//        ) {
//            OptionItem(
//                icon = painterResource(id = R.mipmap.ic_notification),
//                title = stringResource(id = R.string.message_notification),
//                onClick = onNotificationClick,
//                showDivider = false
//            )
//        }
//
//        // 4. 文件和收藏分组
//        Column(
//            modifier = Modifier.padding(top = 16.dp)
//        ) {
//            OptionItem(
//                icon = painterResource(id = R.mipmap.ic_settings_file),
//                title = stringResource(id = R.string.file),
//                onClick = onFileClick
//            )
//
//            OptionItem(
//                icon = painterResource(id = R.mipmap.ic_star),
//                title = stringResource(id = R.string.favorites),
//                onClick = onFavoritesClick
//            )
//        }
//
//        // 5. 主题、语言和设置分组
//        Column(
//            modifier = Modifier.padding(top = 16.dp)
//        ) {
//            OptionItem(
//                icon = painterResource(id = R.mipmap.ic_theme),
//                title = stringResource(id = R.string.theme),
//                onClick = onThemeClick
//            )
//
//            OptionItem(
//                icon = painterResource(id = R.mipmap.ic_lang),
//                title = stringResource(id = R.string.language),
//                onClick = onLanguageClick
//            )
//
//            OptionItem(
//                icon = painterResource(id = R.mipmap.ic_setting),
//                title = stringResource(id = R.string.setting),
//                onClick = onSettingsClick,
//                showDivider = false
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//    }
//}
//
//@Composable
//private fun ProfileHeader(
//    avatar: Painter,
//    name: String,
//    account: String,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .height(100.dp)
//            .clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = null,
//                onClick = onClick
//            )
//            .padding(top = 20.dp, end = 3.dp),
//        verticalAlignment = Alignment.Top
//    ) {
//        // 头像
//        Image(
//            painter = avatar,
//            contentDescription = null,
//            modifier = Modifier
//                .size(60.dp)
//                .clip(CircleShape)
//                .padding(start = 16.dp, end = 16.dp),
//            contentScale = ContentScale.Crop
//        )
//
//        // 姓名和账号
//        Column(
//            modifier = Modifier
//                .weight(1f)
//                .height(70.dp),
//            verticalArrangement = Arrangement.Top
//        ) {
//            Text(
//                text = name,
//                style = MaterialTheme.typography.titleMedium.copy(
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 15.sp
//                ),
//                color = MaterialTheme.colorScheme.onSurface,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis,
//                modifier = Modifier
//                    .padding(top = 15.dp, bottom = 3.dp)
//            )
//
//            Text(
//                text = account,
//                style = MaterialTheme.typography.bodyMedium.copy(
//                    fontSize = 13.sp
//                ),
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis,
//                modifier = Modifier.padding(top = 3.dp)
//            )
//        }
//
//// 右侧箭头
//        Icon(
//            painter = painterResource(id = R.mipmap.ic_right),
//            contentDescription = "更多",
//            modifier = Modifier
//                .padding(top = 15.dp, end = 20.dp)
//                .size(24.dp),
//            tint = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//    }
//}
//
//@Composable
//private fun OptionItem(
//    icon: Painter,
//    title: String,
//    onClick: () -> Unit,
//    showDivider: Boolean = true,
//    modifier: Modifier = Modifier
//) {
//    Column(modifier = modifier.fillMaxWidth()) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp)
//                .clickable(
//                    interactionSource = remember { MutableInteractionSource() },
//                    indication = null,
//                    onClick = onClick
//                )
//                .padding(horizontal = 16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                painter = icon,
//                contentDescription = null,
//                modifier = Modifier.size(24.dp),
//                tint = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//
//            Spacer(modifier = Modifier.width(24.dp))
//
//            Text(
//                text = title,
//                style = MaterialTheme.typography.titleMedium.copy(
//                    fontWeight = FontWeight.Normal,
//                    fontSize = 16.sp
//                ),
//                modifier = Modifier.weight(1f),
//                color = MaterialTheme.colorScheme.onSurface
//            )
//
//            Icon(
//                imageVector = Icons.Default.Edit,
//                contentDescription = "更多",
//                tint = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//
//        if (showDivider) {
//            Divider(
//                modifier = Modifier.padding(start = 72.dp),
//                thickness = 0.5.dp,
//                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreview() {
//    MaterialTheme(
//        colorScheme = lightColorScheme(
//            surface = Color(0xFFFFFFFF),
//            surfaceVariant = Color(0xFFF5F5F5),
//            onSurface = Color(0xFF333333),
//            onSurfaceVariant = Color(0xFF666666),
//            outline = Color(0xFFE0E0E0)
//        )
//    ) {
//        ProfileScreen()
//    }
//}