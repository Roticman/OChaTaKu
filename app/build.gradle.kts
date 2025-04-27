plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
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

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packagingOptions {
        resources {
            excludes += "/google/api/source_info.proto"
            excludes += "/google/api/logging.proto"
            excludes += "/google/api/annotations.proto"
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.core.ktx)
    implementation(libs.core.ktx)
    implementation(libs.volley)
    // Compose BOM & 核心
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Material
    implementation("com.google.android.material:material:1.9.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Firebase App Distribution（注意：版本手写 + 冲突排除）
//    implementation("com.google.firebase:firebase-appdistribution:4.0.0") {
//        exclude(group = "com.google.firebase", module = "protolite-well-known-types")
//    }

    // Coil 图片加载
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Retrofit 网络请求
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    // ViewPager（可选）
    implementation("com.google.accompanist:accompanist-pager:0.25.1")

    implementation("io.socket:socket.io-client:2.1.0")// 注意不兼容 okhttp4，推荐使用这个版本

    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    implementation("androidx.compose.ui:ui-graphics:1.5.0") // 用于绘图操作

    implementation("com.belerweb:pinyin4j:2.5.0")


}
