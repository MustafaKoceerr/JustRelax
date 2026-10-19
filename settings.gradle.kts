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


include(":androidApp")
include(":benchmark")
include(":shared")

include(":core:common")
include(":core:model")
include(":core:playback")
include(":core:ui")
include(":core:presentation")
include(":core:database")
include(":core:network")
include(":core:testing")

include(":core:data")
include(":core:media")
include(":core:analytics")

include(":feature:settings")
include(":feature:scene")
include(":feature:library")
include(":feature:library:ui")
include(":feature:splash")

include(":feature:ai")
include(":feature:saved")
include(":feature:mixer")
include(":feature:timer")
include(":feature:mysounds")
include(":feature:freesound")
