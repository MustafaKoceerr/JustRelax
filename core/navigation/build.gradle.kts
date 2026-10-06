plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

// Routes and navigation state shared by the app shell and features (Navigation 3).
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.findLibrary("navigation3-ui").get())
            api(libs.findLibrary("kotlinx-serialization-json").get())
        }
    }
}
