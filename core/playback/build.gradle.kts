plugins {
    id("justrelax.kmp.library")
    id("justrelax.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:model"))
            implementation(project(":core:data"))

            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.session)
            implementation(libs.androidx.media3.common)
            implementation(libs.kotlinx.coroutines.guava)
        }

        commonTest.dependencies {
            implementation(project(":core:testing"))
        }

        androidHostTest.dependencies {
            implementation(libs.bundles.robolectric.test)
        }
    }
}
