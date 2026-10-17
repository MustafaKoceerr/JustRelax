plugins {
    id("justrelax.kmp.compose")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:common"))
            api(project(":core:model"))

            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.material3.window.size)
        }
    }
}

// Other modules use these strings, fonts and images.
compose.resources {
    publicResClass = true
    packageOfResClass = "com.mustafakoceerr.justrelax.core.ui.generated.resources"
    generateResClass = always
}
