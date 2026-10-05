plugins {
    id("justrelax.kmp.library")
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(project(":core:model"))
            implementation(libs.findLibrary("koin-core").get())
        }

        androidMain.dependencies {
            implementation(libs.findLibrary("androidx-core-ktx").get())
            implementation(libs.findLibrary("androidx-appcompat").get())
            implementation(libs.findLibrary("koin-android").get())
        }
    }
}