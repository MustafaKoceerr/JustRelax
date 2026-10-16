plugins {
    id("justrelax.kmp.compose")
    alias(libs.plugins.kotlin.serialization)
}

// Shared app shell (navigation, DI wiring, root composable). The Android entry point lives
// in :androidApp because AGP 9 no longer allows com.android.application in a KMP module.
kotlin {
    if (isIosEnabled) {
        // Linked into the Xcode project by the embedAndSignAppleFrameworkForXcode build phase.
        listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
            target.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            api(project(":core:ui"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:system"))
            implementation(project(":core:audio"))
            implementation(project(":core:navigation"))
            implementation(project(":data:repository"))

            implementation(project(":feature:home"))
            implementation(project(":feature:mixer"))
            implementation(project(":feature:saved"))
            implementation(project(":feature:ai"))
            implementation(project(":feature:timer"))
            implementation(project(":feature:settings"))
            implementation(project(":feature:player"))
            implementation(project(":feature:onboarding"))
            implementation(project(":feature:splash"))

            implementation(libs.bundles.koin.compose)
            implementation(libs.bundles.lifecycle.compose)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)

            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.coil.svg)
        }
    }
}

compose.resources {
    packageOfResClass = "com.mustafakoceerr.justrelax.composeapp.generated.resources"
}
