plugins {
    id("justrelax.kmp.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.mixer"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:audio"))
            implementation(libs.findLibrary("compottie").get())
            implementation(libs.findLibrary("compottie-resources").get())
            implementation(libs.findLibrary("compottie-dot").get())
        }
    }
}
