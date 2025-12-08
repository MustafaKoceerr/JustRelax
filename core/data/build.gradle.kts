plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight) // Veritabanı burada
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.data"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Alt katmanlara erişim
            api(project(":core:common"))
            api(project(":core:model"))
            api(project(":core:domain"))

            // Networking (Ktor)
            implementation(libs.findLibrary("ktor-client-core").get())
            implementation(libs.findLibrary("ktor-client-content-negotiation").get())
            implementation(libs.findLibrary("ktor-serialization-kotlinx-json").get())
            implementation(libs.findLibrary("ktor-client-logging").get())

            // Database (SQLDelight)
            implementation(libs.findLibrary("sqldelight-runtime").get())
            implementation(libs.findLibrary("sqldelight-coroutines-extensions").get())

            // Settings (Key-Value)
            implementation(libs.findLibrary("multiplatform-settings-no-arg").get())
            implementation(libs.findLibrary("multiplatform-settings-coroutines").get())

            // Utils
            implementation(libs.findLibrary("okio").get())
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
            implementation(libs.findLibrary("kotlinx-datetime").get())
            implementation(libs.findLibrary("koin-core").get()) // DI için
        }
        androidMain.dependencies {
            implementation(libs.findLibrary("ktor-client-android").get())
            implementation(libs.findLibrary("sqldelight-android-driver").get())

            implementation(libs.findLibrary("koin-android").get())
        }

        iosMain.dependencies {
            implementation(libs.findLibrary("ktor-client-darwin").get())
            implementation(libs.findLibrary("sqldelight-native-driver").get())
        }
    }
}


// SQLDelight Ayarları (Eski core'dan buraya taşıdık)
sqldelight {
    databases {
        create("JustRelaxDb") {
            packageName.set("com.mustafakoceerr.justrelax.core.database")
        }
    }
}