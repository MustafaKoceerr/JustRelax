plugins {
    id("justrelax.kmp.library")
    id("justrelax.android.library.compose") // UI var
    alias(libs.plugins.kotlin.serialization) // Screen @Serializable olabilir
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.settings"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core Modülleri
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:audio"))
            implementation(project(":core:ui"))   // Theme, LanguageSwitcher için
            implementation(project(":core:navigation")) // AppScreen için

            // --- Compose Kütüphaneleri ---
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            // Koin (ViewModel inject)
            implementation(libs.findLibrary("koin-core").get())
            implementation(libs.findLibrary("koin-compose").get())
            implementation(libs.findLibrary("koin-compose-viewmodel").get())
        }
    }
}