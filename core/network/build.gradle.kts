plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.network"
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
        }

        iosMain.dependencies {
            implementation(libs.findLibrary("ktor-client-darwin").get())
        }
    }
}