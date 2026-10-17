plugins {
    id("justrelax.kmp.feature")
}

android {
    namespace = "com.mustafakoceerr.justrelax.feature.splash"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:domain"))
        }
    }
}
