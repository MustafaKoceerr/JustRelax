plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

// Routes and navigation state of the app shell (Navigation 3).
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.navigation3.ui)
            api(libs.kotlinx.serialization.json)
        }
    }
}
