plugins {
    id("justrelax.kmp.library") // Convention plugin
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))  // Sound, AppLanguage vb.
            api(project(":core:common")) // Result, AppError

            implementation(libs.findLibrary("kotlinx-coroutines-core").get()) // Flow için

            // DI
            implementation(libs.findLibrary("koin-core").get())
    }
}
}