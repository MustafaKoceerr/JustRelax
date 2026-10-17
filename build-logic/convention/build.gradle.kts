plugins {
    `kotlin-dsl`
}

group = "com.mustafakoceerr.justrelax.buildlogic"

dependencies {
    // Plugin kodlarını yazarken ihtiyaç duyacağımız sınıflar (Artifacts)

    // 1. Android Gradle Plugin (LibraryExtension vb. için)
    implementation(libs.android.gradlePlugin)

    // 2. Kotlin Gradle Plugin
    implementation(libs.kotlin.gradlePlugin)

    // 3. Compose Multiplatform Plugin
    implementation(libs.compose.gradlePlugin)

    // 4. Compose Compiler Plugin
    implementation(libs.composeCompiler.gradlePlugin)
}


gradlePlugin {
    plugins {
        // KMP library modülleri: Android + (bayrak açıksa) iOS hedefleri, ortak test bağımlılıkları
        register("kmpLibrary") {
            id = "justrelax.kmp.library"
            implementationClass = "JustRelaxKmpConventionPlugin"
        }

        // UI içeren modüllerde Compose'u açar
        register("androidCompose") {
            id = "justrelax.android.library.compose"
            implementationClass = "JustRelaxComposeConventionPlugin"
        }

        // Feature modülleri: KMP + Compose + ortak core modülleri + Koin + test fake'leri
        register("kmpFeature") {
            id = "justrelax.kmp.feature"
            implementationClass = "JustRelaxFeatureConventionPlugin"
        }
    }
}
