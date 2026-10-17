plugins {
    id("justrelax.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:audio"))
            implementation(libs.coil.compose)
        }
    }
}
