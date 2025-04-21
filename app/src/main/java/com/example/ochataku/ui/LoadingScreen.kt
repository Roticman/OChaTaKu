package com.example.ochataku.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ochataku.R

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)), // 对应 @color/gray5
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Logo 图片 (113dp 顶部间距)
        Image(
            painter = painterResource(R.mipmap.ic_launcher),
            contentDescription = "App Logo",
            modifier = Modifier.padding(top = 113.dp)
        )

        // 应用名称 (28dp 顶部间距)
        Text(
            text = stringResource(R.string.app_name),
            color = Color(0xFF333333), // 对应 @color/black2
            fontSize = 24.sp,
            modifier = Modifier.padding(top = 28.dp)
        )

        // 弹性空白空间 (等效于 Space + layout_weight=1)
        Spacer(modifier = Modifier.weight(1f))

        // 标语 (53dp 底部间距)
        Text(
            text = stringResource(R.string.slogan),
            color = Color(0xFF888888), // 对应 @color/gray11
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 53.dp)
        )
    }
}
