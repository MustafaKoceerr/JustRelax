plugins {
    id("justrelax.kmp.library")
    id("justrelax.android.library.compose")
    alias(libs.plugins.kotlin.serialization)
    kotlin("native.cocoapods")
}

// Shared app shell (navigation, DI wiring, root composable). The Android entry point lives
// in :androidApp because AGP 9 no longer allows com.android.application in a KMP module.
kotlin {
    if (isIosEnabled) {
        cocoapods {
            summary = "JustRelax Shared App"
            homepage = "https://example.com/justrelax"
            version = "1.0.0"
            ios.deploymentTarget = "16.0"
            extraSpecAttributes["libraries"] = "'sqlite3'"

            framework {
                baseName = "ComposeApp"
                isStatic = true
                export(project(":core:ui"))
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

            implementation(libs.findLibrary("koin-core").get())
            implementation(libs.findLibrary("koin-compose").get())
            implementation(libs.findLibrary("koin-compose-viewmodel").get())
            implementation(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
            implementation(libs.findLibrary("androidx-lifecycle-viewmodel-navigation3").get())

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.findLibrary("coil-compose").get())
            implementation(libs.findLibrary("coil-network").get())
            implementation(libs.findLibrary("coil-svg").get())
        }
    }
}

compose.resources {
    packageOfResClass = "com.mustafakoceerr.justrelax.composeapp.generated.resources"
}
