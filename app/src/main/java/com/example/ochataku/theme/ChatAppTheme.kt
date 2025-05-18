package com.example.ochataku.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 动态生成颜色方案
fun dynamicColorScheme(primary: Color, darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = primary,
            onPrimary = Color.Black,
            background = Color(0xFF121212),
            onBackground = Color.White
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            background = Color(0xFFF5F5F5),
            onBackground = Color.Black
        )
    }
}

@Composable
fun ChatAppTheme(
    primaryColor: Color = Color(0xFF6200EE),
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = dynamicColorScheme(primaryColor, darkTheme)

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )
}
