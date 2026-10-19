plugins {
    id("justrelax.kmp.feature.legacy")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:playback"))
        }
    }
}
