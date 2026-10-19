plugins {
    id("justrelax.android.feature.ui")
}

dependencies {
    implementation(project(":core:media"))
    // The system's document picker for audio files.
    implementation(libs.androidx.activity.compose)
}
