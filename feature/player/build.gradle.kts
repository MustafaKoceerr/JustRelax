plugins {
    id("justrelax.kmp.library")
    id("justrelax.android.library.compose")
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.player"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // --- Core Modülleri ---
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))

            // KRİTİK: Ses motoruna erişim
            implementation(project(":core:audio"))
            // Ayrıca indirme işlemi için Data katmanına erişim gerekebilir (SoundDownloader)

            // --- Compose ---
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            // --- Koin ---
            implementation(libs.findLibrary("koin-core").get())
            implementation(libs.findLibrary("koin-compose").get())
            implementation(libs.findLibrary("koin-compose-viewmodel").get())

            // Coil image
            implementation(libs.findLibrary("coil-compose").get())
        }
    }
}