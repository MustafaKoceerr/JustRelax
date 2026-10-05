plugins {
    id("justrelax.kmp.feature")
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.player"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:audio"))
            implementation(libs.findLibrary("coil-compose").get())
        }
    }
}
