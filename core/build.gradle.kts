plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)

    // Core modülümüzün de Compose'u kullanabilmesi için bu plugin'leri ekliyoruz.
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true

        namespace = "com.mustafakoceerr.justrelax.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    // iOS hedefleri (bu kısım şablonun verdiği gibi kalabilir, baseName'i değiştirelim)
    val xcfName = "Core" // Modülün adıyla aynı olması daha anlaşılır

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                // Add KMP dependencies here
                // api: Bu bağımlılıkları, :core modülünü kullanan diğer modüllerin de
                // görmesini sağlar. Bu, 'implementation'dan farklıdır.
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(libs.kotlinx.coroutines.core) // Coroutines'i de dışarıya açalım

                // Navigasyon ve Ayarlar altyapısının ihtiyaç duyduğu kütüphaneler
                // Bunlar 'implementation' olabilir, çünkü feature modülleri doğrudan
                // bu kütüphanelerle konuşmayacak, bizim soyutlamalarımızla konuşacak.
                implementation(libs.voyager.navigator)
                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.multiplatform.settings.coroutines)

                // Koin'in çekirdeği de burada olmalı ki DI yapabilelim.
                implementation(libs.koin.core)

                // Resources'a erişmek için
                api(compose.components.resources)

                // Extended icons
                implementation(compose.materialIconsExtended)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {

                // Multiplatform-Settings'in Android sürücüsü burada olmalı.
                // Android için en modern ses motoru: Media3 (ExoPlayer)
                implementation("androidx.media3:media3-exoplayer:1.8.0")

                implementation(libs.androidx.media)
                implementation(libs.koin.android)
                implementation(libs.core)
                implementation(libs.androidx.appcompat)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.testExt.junit)
            }
        }

        iosMain {
            dependencies {
                // Multiplatform-Settings'in iOS sürücüsü burada olmalı.
            }
        }
    }
}



// BU KISMI KONTROL ET VE GÜNCELLE:
compose.resources {
    // Kaynakların paket adı (Senin importlarınla uyuşmalı)
    packageOfResClass = "com.mustafakoceerr.justrelax.core.generated.resources"

    // KRİTİK AYAR: Kaynakları diğer modüllerin (composeApp) görmesi için public yap.
    publicResClass = true

    // Eğer generateResClass sorunu varsa bunu da ekle:
    generateResClass = always
}

