import co.touchlab.skie.configuration.DefaultArgumentInterop

plugins {
    id("justrelax.kmp.library")
    id("justrelax.koin")
    alias(libs.plugins.skie)
}

// What both apps share: the Koin graph and app-wide state. Android draws it in :androidApp, iOS in SwiftUI;
// the iOS app links this module as the "Shared" framework.

// The modules Swift sees in the framework.
val exportedProjects = listOf(
    ":core:analytics",
    ":core:model",
    ":core:data",
    ":core:presentation",
    // The sound editor's TrimRange and TrimProblem.
    ":core:media",
    ":feature:library",
    ":feature:mixer",
    ":feature:saved",
    ":feature:ai",
    ":feature:timer",
    ":feature:settings",
    ":feature:scene",
    ":feature:splash",
    ":feature:mysounds",
    ":feature:freesound",
)

skie {
    features {
        // Kotlin's default arguments become Swift overloads (Kotlin/Native alone requires every
        // argument). Only for our own code: overloads for library functions (kotlinx-datetime)
        // fail to link.
        group("com.mustafakoceerr.justrelax") {
            DefaultArgumentInterop.Enabled(true)
        }
    }
    // SKIE would otherwise send anonymous build statistics to Touchlab.
    analytics {
        enabled.set(false)
    }
}

kotlin {
    // The iOS targets come from the convention; each is linked into the Xcode project by the
    // embedAndSignAppleFrameworkForXcode build phase.
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries.framework {
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", "com.mustafakoceerr.justrelax.shared")
            // The shared ViewModels and their state, the models they carry, and AnalyticsHelper and
            // CrashReporter, which Swift implements with the Firebase iOS SDK.
            exportedProjects.forEach { export(project(it)) }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))
            implementation(project(":core:playback"))
            exportedProjects.forEach { api(project(it)) }

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.koin.core.viewmodel)
        }
    }
}
