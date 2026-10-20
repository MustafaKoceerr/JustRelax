plugins {
    id("justrelax.android.feature.ui")
}

dependencies {
    implementation(project(":core:media"))
    // The system's document picker for audio files.
    implementation(libs.androidx.activity.compose)

    // Layout tests run the screens on Robolectric.
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
