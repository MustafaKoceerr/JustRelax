plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.kotlin.serialization)
    // 1. Eklentiyi buraya ekle
    alias(libs.plugins.buildConfig)
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.network"
}
// BU BLOĞU EKLE:
buildConfig {
    // Üretilecek BuildConfig.kt dosyasının paket adı
    packageName("com.mustafakoceerr.justrelax.core.network")

    // Release ve debug sürümleri farklı olabilir. kullanılan url'ler farklı olabilir.
    // bu yüzden buildconfig'den yönetiyoruz. release'e çıkarken remoteConfig'e alacağız.
    buildConfigField("String", "SOUNDS_URL", "\"https://pub-728a358af0b143fcbf9aa1e060e0dfa9.r2.dev/config.json\"")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Sunucudan gelen verileri hangi data class'a dönüştüreceğini bilmeli
            api(project(":core:model"))
            // Result, AppError gibi yapıları kullanacak
            api(project(":core:common"))
            api(project(":core:domain"))

            // Ktor kütüphaneleri
            implementation(libs.findLibrary("ktor-client-core").get())
            implementation(libs.findLibrary("ktor-client-content-negotiation").get())
            implementation(libs.findLibrary("ktor-serialization-kotlinx-json").get())
            implementation(libs.findLibrary("ktor-client-logging").get()) // İstekleri loglamak için

            // Koin (DI)
            implementation(libs.findLibrary("koin-core").get())
        }

        androidMain.dependencies {
            implementation(libs.findLibrary("ktor-client-android").get())
            // YENİ: Ktor için OkHttp motorunu ekliyoruz
            implementation(libs.findLibrary("ktor-client-okhttp").get())
        }

        iosMain.dependencies {
            implementation(libs.findLibrary("ktor-client-darwin").get())
        }
    }
}