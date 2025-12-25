/*
Görevi: Context (Android) veya UIKit (iOS) gibi en temel platform API'lerine ihtiyaç duyan, ama UI çizmeyen,
"sistemle konuşan" tüm yardımcı sınıfları barındırmak.
 */
plugins {
    id("justrelax.kmp.library")
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.system"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Domain'deki arayüzleri implemente edecek
            implementation(project(":core:domain"))
            implementation(project(":core:model"))

            // Koin (DI için)
            implementation(libs.findLibrary("koin-core").get())
        }

        androidMain.dependencies {
            // Android Context'e ve Intent'lere erişim için
            implementation(libs.findLibrary("androidx-core-ktx").get())
            // Dil değişimi için
            implementation(libs.findLibrary("androidx-appcompat").get())
            // Koin Android Context
            implementation(libs.findLibrary("koin-android").get())
        }

    }
}