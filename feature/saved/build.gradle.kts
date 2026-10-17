plugins {
    id("justrelax.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:playback"))
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            implementation(libs.kotlinx.datetime)
        }
    }
}
