plugins {
    id("justrelax.kmp.library")
    id("justrelax.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
