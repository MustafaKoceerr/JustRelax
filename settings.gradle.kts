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
include(":core:ui")
include(":core:navigation")
include(":core:domain")
include(":core:database")
include(":core:network")
include(":core:system")

include(":data:repository")

include(":feature:settings")
include(":feature:player")
include(":feature:home")
include(":feature:onboarding")
include(":feature:splash")

include(":feature:ai")
//include(":feature:saved")
include(":feature:mixer")
//include(":feature:timer")
