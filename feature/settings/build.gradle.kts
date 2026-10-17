plugins {
    id("justrelax.kmp.feature")
    alias(libs.plugins.kotlin.serialization)
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:audio"))
        }
    }
}
