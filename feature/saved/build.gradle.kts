plugins {
    id("justrelax.kmp.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.saved"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:audio"))
            implementation(libs.findLibrary("coil-compose").get())
            implementation(libs.findLibrary("coil-network").get())
            implementation(libs.findLibrary("kotlinx-datetime").get())
        }
    }
}
