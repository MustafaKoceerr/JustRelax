plugins {
    id("justrelax.kmp.library")
    id("justrelax.android.library.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.saved"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // --- Core ---
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))
            implementation(project(":core:data")) // Repository erişimi
            implementation(project(":core:audio")) // SoundManager erişimi

            // --- Compose ---
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)

            // --- Koin & Voyager ---
            implementation(libs.findLibrary("koin-core").get())
            implementation(libs.findLibrary("koin-compose").get())
            implementation(libs.findLibrary("koin-compose-viewmodel").get())
            implementation(libs.findLibrary("voyager-screenmodel").get())

            // --- Coil3 ---
            implementation(libs.findLibrary("coil-compose").get())
            implementation(libs.findLibrary("coil-network").get())
            implementation(libs.findLibrary("coil-compose").get())
            implementation(libs.findLibrary("coil-network").get())
        }
    }
}