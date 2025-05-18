package com.example.ochataku.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ochataku.R
import com.example.ochataku.viewmodel.ThemeViewModel

@Composable
fun ThemeSwitchScreen(navController: NavController) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val currentColor by themeViewModel.primaryColor.collectAsState()

    val themeColors = listOf(
        Color(0xFF6200EE), // 紫
        Color(0xFF03DAC5), // 青绿
        Color(0xFFFF5722), // 橙
        Color(0xFF4CAF50), // 绿
        Color(0xFF2196F3)  // 蓝
    )

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(stringResource(R.string.select_theme_color), fontSize = 20.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            themeColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(color)
                        .clickable {
                            themeViewModel.updatePrimaryColor(color)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (color == currentColor) stringResource(R.string.current_theme) else stringResource(
                            R.string.apply_this_theme
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
