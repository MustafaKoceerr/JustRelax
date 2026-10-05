import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Android entry point: Application, Activity, playback service and app resources.
// Shared UI and logic come from :composeApp. Kotlin is built into AGP 9, so no kotlin-android plugin.
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "com.mustafakoceerr.justrelax"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.mustafakoceerr.justrelax"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 5
        versionName = "1.2.0"
    }

    buildTypes {
        getByName("release") {
            // R8: kod küçültme + obfuscation
            isMinifyEnabled = true
            // Kullanılmayan kaynakları siler
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        // Geliştirme yaparken hızlı derlensin diye debug'da kapalı kalsın
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            // Compose resources are read from assets first; the Java-resource copy is only a duplicate.
            excludes += "composeResources/**"
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.core.audio)
    implementation(projects.core.domain)
    implementation(projects.core.network)

    implementation(libs.koin.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)

    // Playback service (Media3 session + notification)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)
    implementation(libs.kotlinx.coroutines.guava)

    testImplementation(projects.core.testing)
    // Types whitelisted in KoinGraphTest (created inside lambdas, not via constructors)
    testImplementation(projects.core.database)
    testImplementation(libs.sqldelight.runtime)
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.kotlinx.coroutines.test)
}
