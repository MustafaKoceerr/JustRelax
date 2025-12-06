import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    // Gerekli plugin'leri toml'dan alıyoruz
    alias(libs.plugins.kotlin.serialization)

    kotlin("native.cocoapods")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()


    // CocoaPods entegrasyonu
    cocoapods {
        summary = "Shared module for JustRelax app"
        homepage = "https://example.com/justrelax"
        version = "1.0.0"

        ios.deploymentTarget = "16.0"
        // --- ÇÖZÜM BURASI ---
        // Bu satır, oluşturulan .podspec dosyasına
        // "spec.libraries = 'sqlite3'" satırını ekler.
        // Böylece 'pod install' dediğinde Xcode otomatik olarak sqlite3'ü bağlar.
        extraSpecAttributes["libraries"] = "'sqlite3'"

        framework {
            baseName = "ComposeApp"
            isStatic = true // Compose için statik olması önerilir

            // Eğer export ettiğin kütüphaneler varsa buraya eklersin
            // --- ÇÖZÜM BURASI ---
            // iOS sistemindeki SQLite3 kütüphanesini linkle
//            linkerOpts.add("-lsqlite3")
             export(projects.core)

            // export(libs.someLibrary)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.android)
            implementation(libs.sqldelight.android.driver)

        }
        commonMain.dependencies {
            api(project(":core"))

            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Koin
            // Koin core
            implementation(libs.koin.core)
            implementation(libs.koin.core.coroutines)
            // Compose Multiplatform Koin API
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Voyager
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)
            implementation(libs.voyager.tab.navigator)
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // Kotlinx Serialization
            implementation(libs.kotlinx.serialization.json)

            // Multiplatform Settings
            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.coroutines)

            // SQLDelight
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)

            // Extended icons
            implementation(compose.materialIconsExtended)

            // RepositoryImpl içinde tarih (Clock.System.now) kullanmak için:
            implementation(libs.kotlinx.datetime)

            // Coil
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.coil.svg) // SVG ikonlar için şart

        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

}


android {
    namespace = "com.mustafakoceerr.justrelax"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.mustafakoceerr.justrelax"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}


dependencies {
    debugImplementation(compose.uiTooling)
}

// --- KRİTİK NOKTA ---
compose.resources {
    // Core modülünde: "justrelax.core.generated.resources" demiştik.
    // Burada FARKLI bir isim olmalı. Genellikle şöyledir:
    packageOfResClass = "com.mustafakoceerr.justrelax.composeapp.generated.resources"

    // Uygulamanın ana modülü olduğu için public yapmana gerek yok (default false kalabilir)
    // generateResClass = always
}

