// Freesound: search CC0 and CC-BY sounds, listen to them and add them through the editor.
plugins {
    id("justrelax.kmp.feature.legacy")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:media"))
        }
    }
}
