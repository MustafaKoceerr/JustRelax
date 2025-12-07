plugins {
    id("justrelax.kmp.library")
    id("justrelax.android.library.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.main"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // --- Core ---
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))
            implementation(project(":core:data")) // SyncManager için
            implementation(project(":core:audio"))

            // --- TÜM FEATURE'LAR (Tab'ları oluşturmak için) ---
            implementation(project(":feature:home"))
            implementation(project(":feature:mixer"))
            implementation(project(":feature:saved"))
            implementation(project(":feature:ai"))
            implementation(project(":feature:timer"))
            implementation(project(":feature:player")) // PlayerBar için

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
            implementation(libs.findLibrary("voyager-navigator").get())
            implementation(libs.findLibrary("voyager-tab-navigator").get())
            implementation(libs.findLibrary("voyager-screenmodel").get())
        }
    }
}