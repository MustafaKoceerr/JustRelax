plugins {
    id("justrelax.kmp.feature")
}


kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
            implementation(libs.findLibrary("compottie").get())
            implementation(libs.findLibrary("compottie-resources").get())
            implementation(libs.findLibrary("compottie-dot").get())
        }
    }
}
