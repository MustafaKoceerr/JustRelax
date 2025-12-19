import java.util.Properties
plugins {
    id("justrelax.kmp.library")
    id("justrelax.android.library.compose")
    alias(libs.plugins.kotlin.serialization) // JSON parse için şart
    // 1. Eklentiyi buraya ekle
    alias(libs.plugins.buildConfig)
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.ai"
}
// 2. local.properties dosyasını okuma mantığı
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

// 3. BuildConfig Konfigürasyonu
buildConfig {
    // Oluşacak sınıfın paket adı (AiServiceImpl ile aynı olsun ki import kolay olsun)
    packageName("com.mustafakoceerr.justrelax.feature.ai")

    // Key'i tanımlıyoruz
    buildConfigField(
        "String", // Tipi
        "GEMINI_API_KEY", // Kodda kullanacağımız isim
        "\"${localProperties.getProperty("GEMINI_API_KEY")}\"" // Değer (Tırnaklara dikkat)
    )
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            // --- Core ---
            implementation(project(":core:common"))
            implementation(project(":core:model"))
            implementation(project(":core:ui"))
            implementation(project(":core:navigation"))
            implementation(project(":core:audio")) // Mix'i çalmak için (SoundManager)

            // --- Compose ---
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.uiToolingPreview)

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