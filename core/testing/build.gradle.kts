plugins {
    id("justrelax.kmp.library")
}

// Fakes and test utilities; only test source sets depend on this module.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(libs.kotlinx.coroutines.test)
        }
    }
}
