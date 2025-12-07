plugins {
    id("justrelax.kmp.library")
    id("justrelax.android.library.compose")
    alias(libs.plugins.kotlin.serialization) // JSON parse için şart
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.ai"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // --- Core ---
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))
            implementation(project(":core:data")) // SoundRepository erişimi için
            implementation(project(":core:audio")) // Mix'i çalmak için (SoundManager)

            // --- Compose ---
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // --- Network & Serialization (Gemini API için) ---
            implementation(libs.findLibrary("ktor-client-core").get())
            implementation(libs.findLibrary("ktor-client-content-negotiation").get())
            implementation(libs.findLibrary("ktor-serialization-kotlinx-json").get())
            implementation(libs.findLibrary("kotlinx-serialization-json").get())

            // --- Koin & Voyager ---
            implementation(libs.findLibrary("koin-core").get())
            implementation(libs.findLibrary("koin-compose").get())
            implementation(libs.findLibrary("koin-compose-viewmodel").get())
            implementation(libs.findLibrary("voyager-screenmodel").get())
        }
    }
}