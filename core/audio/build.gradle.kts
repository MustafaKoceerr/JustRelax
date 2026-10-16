plugins {
    id("justrelax.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:model"))
            api(project(":core:domain"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.koin.android)
            implementation(libs.androidx.core.ktx)
        }

        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}
