// Android entry point: Application, Activity, playback service and app resources.
// The Compose UI and navigation live here; shared logic comes from :shared and the feature modules.
import java.util.Properties

plugins {
    id("justrelax.android.application")
    alias(libs.plugins.kotlin.serialization)
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
        versionCode = 6
        versionName = "1.3.0"
        buildConfigField(
            "long",
            "PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER",
            "${localProperties.getProperty("PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER") ?: "0"}L",
        )
    }

    // The Play upload key, read from local.properties (RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD,
    // RELEASE_KEY_ALIAS, optional RELEASE_KEY_PASSWORD). Without them release builds unsigned, as on CI.
    val releaseStoreFile = localProperties.getProperty("RELEASE_STORE_FILE")?.let(::file)
    val releaseSigning = releaseStoreFile?.let { storeFile ->
        if (!storeFile.exists()) throw GradleException("RELEASE_STORE_FILE not found: $storeFile")
        val storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
            ?: throw GradleException("RELEASE_STORE_PASSWORD is missing in local.properties")
        signingConfigs.create("release") {
            this.storeFile = storeFile
            this.storePassword = storePassword
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
                ?: throw GradleException("RELEASE_KEY_ALIAS is missing in local.properties")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD") ?: storePassword
        }
    }

    buildTypes {
        getByName("release") {
            releaseSigning?.let { signingConfig = it }
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

}

// The benchmark builds get their own application id, so they install beside the Play app, which has a
// different signature. Set on the variant: the plugin copies release into these build types late,
// over a DSL applicationIdSuffix.
androidComponents {
    onVariants(selector().withBuildType("benchmarkRelease")) { it.applicationId.set("com.mustafakoceerr.justrelax.benchmark") }
    onVariants(selector().withBuildType("nonMinifiedRelease")) { it.applicationId.set("com.mustafakoceerr.justrelax.benchmark") }
}

dependencies {
    implementation(projects.shared)
    implementation(projects.core.common)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.core.presentation)
    implementation(projects.core.analytics)
    implementation(projects.core.playback)
    implementation(projects.core.data)
    implementation(projects.core.network)

    implementation(projects.feature.library.ui)
    implementation(projects.feature.mixer.ui)
    implementation(projects.feature.saved.ui)
    implementation(projects.feature.ai.ui)
    implementation(projects.feature.timer.ui)
    implementation(projects.feature.settings.ui)
    implementation(projects.feature.scene.ui)
    implementation(projects.feature.splash.ui)
    implementation(projects.feature.mysounds.ui)
    implementation(projects.feature.freesound.ui)

    // Navigation: a back stack of serializable routes, ViewModels scoped to each destination.
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.bundles.androidx.lifecycle.compose)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.koin.androidx.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koin.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    // Installs the Baseline Profile on devices without Play's cloud profiles (sideloads, benchmarks).
    implementation(libs.androidx.profileinstaller)
    baselineProfile(projects.benchmark)

    // Playback service (Media3 session + notification)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)

    testImplementation(projects.core.testing)
    // Types whitelisted in KoinGraphTest (created inside lambdas, not via constructors)
    testImplementation(projects.core.database)
    testImplementation(libs.sqldelight.runtime)
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.koin.test)
    testImplementation(libs.bundles.robolectric.test)
    testImplementation(libs.kotlinx.coroutines.test)
}
