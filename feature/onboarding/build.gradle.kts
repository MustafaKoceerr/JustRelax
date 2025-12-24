plugins {
    id("justrelax.kmp.library")
    id("justrelax.android.library.compose")
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.onboarding"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // --- Core Modülleri ---
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))

            // YENİ EKLENENLER:

            // 1. Domain Katmanı (UseCase'leri kullanmak için)
            implementation(project(":core:domain"))

            // 2. Compose (UI çizmek için)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            // --- Koin & Voyager ---
            implementation(libs.findLibrary("koin-core").get())
            implementation(libs.findLibrary("koin-compose").get())
            implementation(libs.findLibrary("koin-compose-viewmodel").get())
            implementation(libs.findLibrary("voyager-screenmodel").get())
            implementation(libs.findLibrary("voyager-navigator").get())        }
    }
}