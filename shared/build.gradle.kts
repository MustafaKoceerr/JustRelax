plugins {
    id("justrelax.kmp.library")
    id("justrelax.koin")
}

// What both apps share: the Koin graph and app-wide state. Android draws it in :androidApp, iOS in SwiftUI;
// the iOS app links this module as the "Shared" framework.
kotlin {
    // The iOS targets come from the convention; each is linked into the Xcode project by the
    // embedAndSignAppleFrameworkForXcode build phase.
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
            // Swift implements AnalyticsHelper and CrashReporter with the Firebase iOS SDK.
            export(project(":core:analytics"))
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:presentation"))
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

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.koin.core.viewmodel)
        }
    }
}
