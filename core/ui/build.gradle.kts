plugins {
    id("justrelax.kmp.compose")
    id("justrelax.koin")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:model"))
            api(project(":core:presentation"))

            api(libs.compose.material3.adaptive)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
        }
    }
}

// Other modules use these strings, fonts and images.
compose.resources {
    publicResClass = true
    generateResClass = always
}
