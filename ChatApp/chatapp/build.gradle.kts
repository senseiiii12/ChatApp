import org.apache.tools.ant.util.JavaEnvUtils.VERSION_1_8

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    kotlin("kapt")
    id ("dagger.hilt.android.plugin")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.chatapp.chatapp"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.chatapp.chatapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
//    implementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform("androidx.compose:compose-bom:2023.10.00"))


    // Compose анимации
    implementation("androidx.compose.animation:animation:1.0.5")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth:21.1.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-auth") // Дублирующая зависимость, можно удалить

    // Firebase Firestore (База данных) и Storage (Хранилище файлов)
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")

    // Управление статус-баром и системными цветами
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")

    // Dagger - Hilt (Dependency Injection)
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Поддержка корутин
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Coil (Загрузка изображений)
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Toasty (Кастомные всплывающие сообщения)
    implementation("com.github.GrenderG:Toasty:1.5.2")

    // Jetpack Navigation для Compose
    implementation("androidx.navigation:navigation-compose:2.5.3")

    // Экран загрузки (Splash Screen API)
    implementation("androidx.core:core-splashscreen:1.0.0")

    // Gson (Работа с JSON)
    implementation("com.google.code.gson:gson:2.8.7")

    // Инструменты трассировки (Performance Profiler)
    implementation("androidx.tracing:tracing-perfetto:1.0.0")
    implementation("androidx.tracing:tracing-perfetto-binary:1.0.0")

    // WorkManager (Фоновая работа)
    implementation("androidx.work:work-runtime-ktx:2.8.0")

    implementation ("com.google.accompanist:accompanist-navigation-animation:0.32.0")

    implementation("com.google.firebase:firebase-crashlytics:19.4.3")
    implementation("com.google.firebase:firebase-analytics:21.5.0")

    implementation("com.github.senseiiii12:CustomSnackBarLibrary:v0.1.4")






}

