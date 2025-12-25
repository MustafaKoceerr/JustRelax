plugins {
    id("justrelax.kmp.library") // Senin yazdığın convention plugin
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.common"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Flow ve Coroutine işlemleri için gerekli
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
            // Koin (DI)
            implementation(libs.findLibrary("koin-core").get())
        }
    }
}