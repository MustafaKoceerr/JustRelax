plugins {
    id("justrelax.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(libs.compottie)
            implementation(libs.compottie.resources)
            implementation(libs.compottie.dot)
        }
    }
}
