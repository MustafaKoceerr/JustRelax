plugins {
    id("justrelax.kmp.library")
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:model"))
            api(project(":core:domain"))

            implementation(libs.findLibrary("kotlinx-coroutines-core").get())
            implementation(libs.findLibrary("koin-core").get())
        }

        androidMain.dependencies {
            // Android Ses Motoru (ExoPlayer / Media3)
            implementation(libs.findLibrary("androidx-media3-exoplayer").get())
            implementation(libs.findLibrary("koin-android").get())
            implementation(libs.findLibrary("androidx-core-ktx").get())
        }

        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}