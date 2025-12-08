plugins {
    // Yazdığımız Convention Plugin'i kullanıyoruz!
    // Bu plugin senin attığın o uzun android/kotlin ayarlarını otomatik yapıyor.
    id("justrelax.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.common"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Common sadece bunları kullanıyor
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
            implementation(libs.findLibrary("kotlinx-datetime").get())
            implementation(libs.findLibrary("kotlinx-serialization-json").get())
        }
    }
}