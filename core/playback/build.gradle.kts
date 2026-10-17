plugins {
    id("justrelax.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:model"))
            implementation(project(":core:data"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.session)
            implementation(libs.androidx.media3.common)
            implementation(libs.kotlinx.coroutines.guava)
            implementation(libs.koin.android)
            implementation(libs.androidx.core.ktx)
        }

        commonTest.dependencies {
            implementation(project(":core:testing"))
        }
    }
}
