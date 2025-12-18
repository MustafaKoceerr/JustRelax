plugins {
    id("justrelax.kmp.library")
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common")) // Result, Dispatcher vb.
            api(project(":core:model"))  // Sound, SavedMix vb.

            implementation(libs.findLibrary("kotlinx-coroutines-core").get()) // Flow için
            implementation(libs.findLibrary("okio").get())
        }
    }
}