rootProject.name = "JustRelax"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    // Build Logic'i buraya dahil ediyoruz
    includeBuild("build-logic")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "JustRelax"

include(":composeApp")
include(":core")

include(":core:common")
include(":core:model")
include(":core:audio")
include(":core:data")
include(":core:ui")
include(":core:navigation")

include(":feature:settings")
include(":feature:player")
include(":feature:timer")
include(":feature:ai")
include(":feature:saved")
include(":feature:mixer")
include(":feature:home")
include(":feature:main")

// --- Core Modules ---
// Şimdilik sadece include'ları hazırlayalım, klasörleri sonra dolduracağız.
//include(":core:common")
//include(":core:model")
//include(":core:ui")
//include(":core:data")
//include(":core:audio")
//include(":core:navigation")
//
//// --- Feature Modules ---
//include(":feature:home")
//include(":feature:mixer")
//include(":feature:ai")
//include(":feature:settings")
//include(":feature:timer")
//include(":feature:saved")
//include(":feature:player")