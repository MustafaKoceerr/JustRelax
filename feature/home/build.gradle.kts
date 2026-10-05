plugins {
    id("justrelax.kmp.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.home"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:audio"))
        }
    }
}
