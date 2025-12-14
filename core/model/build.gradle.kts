plugins {
    id("justrelax.kmp.library")
    alias(libs.plugins.kotlin.serialization) // Model'de @Serializable var
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.model"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("kotlinx-serialization-json").get())
        }
    }
}