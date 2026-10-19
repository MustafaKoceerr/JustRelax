plugins {
    id("justrelax.kmp.library")
}

// Fakes and test utilities; only test source sets depend on this module.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:data"))
            api(project(":core:media"))
            api(project(":core:analytics"))
            api(project(":core:playback"))
            api(project(":core:presentation"))
            api(libs.kotlinx.coroutines.test)
        }
    }
}
