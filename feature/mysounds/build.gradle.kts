// My sounds: the user's own sounds, imported from files and trimmed in an editor.
plugins {
    id("justrelax.kmp.feature")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:media"))
            implementation(project(":core:playback"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
        }
    }
}
