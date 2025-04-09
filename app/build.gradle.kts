plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
    //KAPT 依赖 Java 编译器 API，而 KSP（Kotlin Symbol Processing） 直接解析 Kotlin 代码，更快且避免 JDK 访问问题：

}

android {
    namespace = "com.example.ochataku"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ochataku"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.10.1")
    // 基础 UI 组件
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")

    // 额外支持（可选）
    implementation("androidx.navigation:navigation-compose:2.7.5") // Jetpack Navigation
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0") // ViewModel 支持
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1") // 约束布局

    implementation("com.google.android.material:material:1.9.0") // Material 组件
    implementation("io.coil-kt:coil-compose:2.0.0")
    implementation("androidx.room:room-runtime:2.6.1") // Room 主库
    ksp("androidx.room:room-compiler:2.6.1") // 替换 kapt
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.navigation:navigation-compose:2.7.7")

}

