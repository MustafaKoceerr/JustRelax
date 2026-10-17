plugins {
    id("justrelax.kmp.feature")
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:audio"))
            implementation(libs.compottie)
            implementation(libs.compottie.resources)
            implementation(libs.compottie.dot)
        }
    }
}
