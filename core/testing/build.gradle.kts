plugins {
    id("justrelax.kmp.library")
}

android {
    namespace = "com.mustafakoceerr.justrelax.core.testing"
}

// Sadece test kaynak setlerinden (commonTest, androidUnitTest) bağımlılık olarak kullanılır.
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:domain"))
            api(libs.findLibrary("kotlinx-coroutines-test").get())
        }
    }
}
