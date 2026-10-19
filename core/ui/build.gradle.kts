plugins {
    id("justrelax.android.library")
    id("justrelax.android.compose")
}

// The Android design system: theme, Compose components and their strings. iOS draws its own in SwiftUI.
dependencies {
    api(project(":core:common"))
    api(project(":core:model"))
    api(project(":core:presentation"))
    api(libs.androidx.compose.material3.adaptive)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
}
