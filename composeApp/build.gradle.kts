import dev.icerock.gradle.MRVisibility
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    // Gerekli plugin'leri toml'dan alıyoruz
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.moko.resources)

    kotlin("native.cocoapods")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    // iOS target'ları tanımla
    listOf(
        iosX64(), // Intel Mac'ler için simülatör
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    // CocoaPods entegrasyonu
    cocoapods {
        summary = "Shared module for JustRelax app"
        homepage = "https://example.com/justrelax"
        version = "1.0.0"

        ios.deploymentTarget = "16.0"
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
            implementation(project(":core"))

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

            // Moko
            implementation(libs.moko.resources)
            implementation(libs.moko.resources.compose)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.moko.resources.test)
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

sqldelight {
    databases {
        create("JustRelaxDatabase") {
            packageName.set("com.mustafakoceerr.justrelax.database")
        }
    }
}

multiplatformResources {
    // MR sınıfının paketi – projene göre düzenle
    resourcesPackage.set("com.mustafakoceerr.justrelax.shared.resources")

    // İsteğe bağlı – MR yerine daha anlamlı bir isim istiyorsan:
    // resourcesClassName.set("SharedRes")

    // İsteğe bağlı – public/internal ayarı
    resourcesVisibility.set(MRVisibility.Public)

    // iOS tarafında zaten 16.0 kullanıyorsun, istersen hizalayabilirsin
    iosMinimalDeploymentTarget.set("16.0")
}