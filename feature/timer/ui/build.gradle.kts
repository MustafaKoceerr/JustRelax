plugins {
    id("justrelax.android.feature.ui")
}

dependencies {
    // Layout tests run the screens on Robolectric.
    testImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
