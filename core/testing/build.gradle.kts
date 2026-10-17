plugins {
    id("justrelax.kmp.library")
}

// Fakes and test utilities; only test source sets depend on this module.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:data"))
            api(project(":core:audio"))
            api(project(":core:ui"))
            api(libs.kotlinx.coroutines.test)
        }
    }
}
