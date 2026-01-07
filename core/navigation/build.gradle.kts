plugins {
    id("justrelax.kmp.library")
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.navigation"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.findLibrary("voyager-navigator").get())
            api(libs.findLibrary("voyager-screenmodel").get())
            api(libs.findLibrary("voyager-transitions").get())
            api(libs.findLibrary("voyager-koin").get())
            api(libs.findLibrary("voyager-tab-navigator").get())
        }
    }
}