plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.sqldelight) // Veritabanı burada
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.database"
}

sqldelight {
    databases {
        create("JustRelaxDatabase") {
            // SQLDelight'ın üreteceği Kotlin sınıflarının paketi
            packageName.set("com.mustafakoceerr.justrelax.core.database.db")
        }
    }
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // DB, Model'i bilmek zorunda (SoundEntity -> Sound dönüşümü için)
            api(project(":core:model"))

            implementation(libs.findLibrary("sqldelight-runtime").get())
            implementation(libs.findLibrary("sqldelight-coroutines-extensions").get())

            // Koin (DI)
            implementation(libs.findLibrary("koin-core").get())
            implementation(libs.findLibrary("kotlinx-serialization-json").get())
        }

        androidMain.dependencies {
            // Android'e özel veritabanı sürücüsü
            implementation(libs.findLibrary("sqldelight-android-driver").get())
        }

        iosMain.dependencies {
            // iOS'e özel veritabanı sürücüsü
            implementation(libs.findLibrary("sqldelight-native-driver").get())
        }
    }
}