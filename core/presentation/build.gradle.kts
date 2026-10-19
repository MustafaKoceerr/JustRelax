plugins {
    id("justrelax.kmp.library")
    id("justrelax.koin")
}

// What the shared ViewModels hand to both UIs (Android Compose, iOS SwiftUI): UI models, formatters,
// test tags and system actions. No Compose.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:model"))
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }
    }
}
