// Usage analytics and crash reports behind small interfaces. Firebase on Android; nothing on iOS yet.
plugins {
    id("justrelax.kmp.library")
    id("justrelax.koin")
}

kotlin {
    sourceSets {

        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
        }
    }
}
