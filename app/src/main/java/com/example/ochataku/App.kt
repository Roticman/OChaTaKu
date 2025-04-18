package com.example.ochataku

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // 关键注解：启用 Hilt 的代码生成
class App : Application() {
    // 可以在这里初始化其他全局依赖（如 Timber、WorkManager 等）
    override fun onCreate() {
        super.onCreate()
        // 其他初始化代码...
    }
}