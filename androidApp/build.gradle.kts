// Android entry point: Application, Activity, playback service and app resources.
// Shared UI and logic come from :composeApp.
plugins {
    id("justrelax.android.application")
}

android {
    namespace = "com.mustafakoceerr.justrelax"

    defaultConfig {
        applicationId = "com.mustafakoceerr.justrelax"
        versionCode = 5
        versionName = "1.2.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    buildFeatures {
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

dependencies {
    implementation(projects.composeApp)
    implementation(projects.core.playback)
    implementation(projects.core.data)
    implementation(projects.core.network)

    implementation(libs.koin.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)

    // Playback service (Media3 session + notification)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)
    implementation(libs.kotlinx.coroutines.guava)

    // The debug sound pack's catalog is read from assets (BuildTypeModule).
    debugImplementation(libs.kotlinx.serialization.json)

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
