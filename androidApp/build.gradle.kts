// Android entry point: Application, Activity, playback service and app resources.
// Shared UI and logic come from :composeApp.
import java.util.Properties

plugins {
    id("justrelax.android.application")
    alias(libs.plugins.baselineprofile)
}

// Firebase (Crashlytics, Analytics) needs the project's google-services.json, which is not in git.
// Without it the app builds and runs, and core:analytics sends nothing.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

// Not secret, but specific to the Play Console account: the Google Cloud project linked for Play Integrity.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.mustafakoceerr.justrelax"

    defaultConfig {
        applicationId = "com.mustafakoceerr.justrelax"
        versionCode = 5
        versionName = "1.2.0"
        buildConfigField(
            "long",
            "PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER",
            "${localProperties.getProperty("PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER") ?: "0"}L",
        )
    }

    buildTypes {
        getByName("release") {
            buildConfigField("String", "API_BASE_URL", "\"https://api.mustafakoceerr.com/\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("debug") {
            // The local API proxy (npm run dev in server/api-proxy), reached with `adb reverse tcp:8787 tcp:8787`.
            buildConfigField("String", "API_BASE_URL", "\"http://localhost:8787/\"")
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    buildFeatures {
        buildConfig = true
    }

    // The Baseline Profile plugin adds benchmarkRelease and nonMinifiedRelease, copies of release that
    // the :benchmark module installs. They are signed with the debug key and share release's code.
    buildTypes.configureEach {
        if (name == "benchmarkRelease" || name == "nonMinifiedRelease") {
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    sourceSets.configureEach {
        if (name == "benchmarkRelease" || name == "nonMinifiedRelease") {
            kotlin.directories += "src/release/kotlin"
        }
    }

    packaging {
        resources {
            // Compose resources are read from assets first; the Java-resource copy is only a duplicate.
            excludes += "composeResources/**"
        }
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
    // MainActivity turns test tags into resource ids for the benchmarks.
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    // Installs the Baseline Profile on devices without Play's cloud profiles (sideloads, benchmarks).
    implementation(libs.androidx.profileinstaller)
    baselineProfile(projects.benchmark)

    // Playback service (Media3 session + notification)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)

    // The debug sound pack's catalog is read from assets (BuildTypeModule).
    debugImplementation(libs.kotlinx.serialization.json)

    testImplementation(projects.core.testing)
    // Types whitelisted in KoinGraphTest (created inside lambdas, not via constructors)
    testImplementation(projects.core.database)
    testImplementation(libs.sqldelight.runtime)
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.koin.test)
    testImplementation(libs.bundles.robolectric.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
