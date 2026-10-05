plugins {
    id("justrelax.kmp.library")
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
            implementation(libs.findLibrary("koin-core").get())
        }
    }
}