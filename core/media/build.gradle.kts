// Audio files on the device: waveforms, trimming and previews for the user's own sounds.
plugins {
    id("justrelax.kmp.library")
    id("justrelax.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(project(":core:common"))
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.media3.common)
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.transformer)
        }
    }
}
