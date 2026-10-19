plugins {
    id("justrelax.kmp.compose")
    alias(libs.plugins.kotlin.serialization)
}

// Shared app shell (navigation, DI wiring, root composable). The Android entry point lives
// in :androidApp because AGP 9 no longer allows com.android.application in a KMP module.
kotlin {
    // The iOS targets come from the convention; each is linked into the Xcode project by the
    // embedAndSignAppleFrameworkForXcode build phase.
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            // Swift implements AnalyticsHelper and CrashReporter with the Firebase iOS SDK.
            export(project(":core:analytics"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            api(project(":core:ui"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:playback"))
            implementation(project(":core:data"))
            implementation(project(":core:media"))
            api(project(":core:analytics"))

            implementation(project(":feature:library"))
            implementation(project(":feature:mixer"))
            implementation(project(":feature:saved"))
            implementation(project(":feature:ai"))
            implementation(project(":feature:timer"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:scene"))
            implementation(project(":feature:splash"))
            implementation(project(":feature:mysounds"))
            implementation(project(":feature:freesound"))

            implementation(libs.bundles.koin.compose)
            implementation(libs.bundles.lifecycle.compose)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.navigation3.ui)
            implementation(libs.compose.material3.adaptive.navigation.suite)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

