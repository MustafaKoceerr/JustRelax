plugins {
    id("justrelax.android.feature.ui")
}

dependencies {
    // The greeting reads the hour in the device's time zone.
    implementation(libs.kotlinx.datetime)
}
