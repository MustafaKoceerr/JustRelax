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
        // 1. Sadece Android Library Modülleri İçin (Eski usül Android-only modüller)
        register("androidLibrary") {
            id = "justrelax.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }

        // 2. KMP Library Modülleri İçin (Core ve Feature modüllerinin %90'ı bunu kullanacak)
        // Hem Android hem iOS hedeflerini otomatik ayarlar.
        register("kmpLibrary") {
            id = "justrelax.kmp.library"
            implementationClass = "JustRelaxKmpConventionPlugin"
        }

        // 3. Compose Kullanan Modüller İçin (UI içerenler)
        // Compose'u aktif eder.
        register("androidCompose") {
            id = "justrelax.android.library.compose"
            implementationClass = "JustRelaxComposeConventionPlugin"
        }
    }
}