// My sounds: the user's own sounds, imported from files and trimmed in an editor.
plugins {
    id("justrelax.kmp.feature.legacy")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:media"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
    }
}
