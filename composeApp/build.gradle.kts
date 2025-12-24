import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
    kotlin("native.cocoapods")
}
android {
    namespace = "com.mustafakoceerr.justrelax"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.mustafakoceerr.justrelax"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // ✅ EKLENDİ: Compose'u manuel açıyoruz
    buildFeatures {
        compose = true
    }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iOS Hedefleri (Plugin olmadığı için manuel ekliyoruz)
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "JustRelax Shared App"
        homepage = "https://example.com/justrelax"
        version = "1.0.0"
        ios.deploymentTarget = "16.0"
        extraSpecAttributes["libraries"] = "'sqlite3'"

        framework {
            baseName = "ComposeApp"
            isStatic = true

            // Core UI ve Main Feature'ı dışarı açıyoruz
            export(project(":core:ui"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            // --- MODÜLLER ---
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:ui"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:system"))

            implementation(project(":core:audio"))
            implementation(project(":core:navigation"))

            implementation(project(":feature:home"))
//            implementation(project(":feature:mixer"))
//            implementation(project(":feature:saved"))
//            implementation(project(":feature:ai"))
//            implementation(project(":feature:timer"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:player"))
            implementation(project(":feature:onboarding"))
            implementation(project(":feature:splash"))
            implementation(project(":data:repository"))

            // --- Koin ---
            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            // --- Compose ---
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.uiToolingPreview)

            // Coil (Resim Yükleme)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.coil.svg)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)

            // Android Ses Motoru (ExoPlayer / Media3)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.session)
            implementation(libs.androidx.media3.common)
        }
    }
}

// --- KRİTİK NOKTA ---
compose.resources {
    // Core modülünde: "justrelax.core.generated.resources" demiştik.
    // Burada FARKLI bir isim olmalı. Genellikle şöyledir:
    packageOfResClass = "com.mustafakoceerr.justrelax.composeapp.generated.resources"

    // Uygulamanın ana modülü olduğu için public yapmana gerek yok (default false kalabilir)
    // generateResClass = always
}

