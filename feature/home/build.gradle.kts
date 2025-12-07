plugins {
    id("justrelax.kmp.library")
    id("justrelax.android.library.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.home"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // --- Core ---
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))
            implementation(project(":core:data")) // Repository
            implementation(project(":core:audio")) // SoundManager

            // --- Feature Dependency (Geçici Teknik Borç) ---
            // Home, PlayerViewModel'i kullanıyorsa:
            implementation(project(":feature:player"))
            // Home, Settings ekranına gitmek istiyorsa (Navigasyon için):
            // implementation(project(":feature:settings")) -> Bunu yapmayacağız, AppNavigator kullanacağız.

            // --- Compose ---
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // --- Koin & Voyager ---
            implementation(libs.findLibrary("koin-core").get())
            implementation(libs.findLibrary("koin-compose").get())
            implementation(libs.findLibrary("koin-compose-viewmodel").get())
            implementation(libs.findLibrary("voyager-screenmodel").get())
            implementation(libs.findLibrary("voyager-navigator").get())
        }
    }
}