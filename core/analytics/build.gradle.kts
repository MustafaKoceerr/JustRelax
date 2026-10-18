// Usage analytics and crash reports behind small interfaces. Firebase on Android; nothing on iOS yet.
plugins {
    id("justrelax.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            implementation(libs.koin.android)
        }
    }
}
