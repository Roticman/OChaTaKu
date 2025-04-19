// 项目根目录的 build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false // Hilt 声明在根项目
    id("com.google.devtools.ksp") version "1.9.21-1.0.16" apply false // KSP 声明在根项目
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")          // Android Gradle 插件
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0") // Kotlin 插件（版本与 KSP 对齐）
    }
}